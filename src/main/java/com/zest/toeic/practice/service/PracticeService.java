package com.zest.toeic.practice.service;

import com.zest.toeic.gamification.service.GamificationService;
import com.zest.toeic.practice.dto.AnswerHistoryResponse;
import com.zest.toeic.practice.dto.AnswerResult;
import com.zest.toeic.practice.dto.SubmitAnswerRequest;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PracticeService {

    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final GamificationService gamificationService;

    public PracticeService(QuestionRepository questionRepository,
                           UserAnswerRepository userAnswerRepository,
                           GamificationService gamificationService) {
        this.questionRepository = questionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.gamificationService = gamificationService;
    }

    public List<Question> getRandomQuestions(Integer part, String difficulty, int limit) {
        List<Question> questions;

        if (part != null && difficulty != null) {
            questions = questionRepository.findByPartAndDifficultyAndStatus(part, difficulty.toUpperCase(), "PUBLISHED");
        } else if (part != null) {
            questions = questionRepository.findByPartAndStatus(part, "PUBLISHED");
        } else {
            questions = questionRepository.findAll();
            questions = questions.stream()
                    .filter(q -> "PUBLISHED".equals(q.getStatus()))
                    .toList();
        }

        var mutableList = new java.util.ArrayList<>(questions);
        Collections.shuffle(mutableList);
        return mutableList.stream().limit(limit).toList();
    }

    public AnswerResult submitAnswer(String userId, SubmitAnswerRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.getQuestionId()));

        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(request.getSelectedOption());
        int xpBase = isCorrect ? 10 : 2;

        // Award XP
        gamificationService.awardXp(userId, xpBase,
                isCorrect ? "ANSWER_CORRECT" : "ANSWER_WRONG",
                request.getQuestionId(),
                "Practice answer — Part " + question.getPart());

        int xpEarned = xpBase;

        UserAnswer answer = UserAnswer.builder()
                .userId(userId)
                .questionId(request.getQuestionId())
                .selectedOption(request.getSelectedOption().toUpperCase())
                .correct(isCorrect)
                .timeTaken(request.getTimeTaken())
                .build();

        userAnswerRepository.save(answer);

        return AnswerResult.builder()
                .questionId(request.getQuestionId())
                .correct(isCorrect)
                .correctAnswer(question.getCorrectAnswer())
                .selectedOption(request.getSelectedOption())
                .xpEarned(xpEarned)
                .build();
    }

    public Question getQuestionById(String id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    public AnswerHistoryResponse getAnswerHistory(String userId, int page, int size) {
        long totalAnswers = userAnswerRepository.countByUserId(userId);
        long correctCount = userAnswerRepository.countByUserIdAndCorrect(userId, true);
        double accuracy = totalAnswers > 0 ? (double) correctCount / totalAnswers * 100 : 0;

        Page<UserAnswer> answersPage = userAnswerRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));

        // Build per-part stats from all answers
        List<UserAnswer> allAnswers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<Integer, AnswerHistoryResponse.PartStats> partStats = buildPartStats(allAnswers);

        // Build recent answer details
        List<AnswerHistoryResponse.AnswerDetail> recentAnswers = answersPage.getContent().stream()
                .map(a -> {
                    Question q = questionRepository.findById(a.getQuestionId()).orElse(null);
                    return AnswerHistoryResponse.AnswerDetail.builder()
                            .answerId(a.getId())
                            .questionId(a.getQuestionId())
                            .part(q != null ? q.getPart() : 0)
                            .selectedOption(a.getSelectedOption())
                            .correctAnswer(q != null ? q.getCorrectAnswer() : "")
                            .correct(a.isCorrect())
                            .timeTaken(a.getTimeTaken())
                            .answeredAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
                            .build();
                })
                .toList();

        return AnswerHistoryResponse.builder()
                .totalAnswers(totalAnswers)
                .correctCount(correctCount)
                .accuracy(Math.round(accuracy * 100.0) / 100.0)
                .partStats(partStats)
                .recentAnswers(recentAnswers)
                .build();
    }

    private Map<Integer, AnswerHistoryResponse.PartStats> buildPartStats(List<UserAnswer> answers) {
        Map<Integer, AnswerHistoryResponse.PartStats> result = new LinkedHashMap<>();

        // Group answers by question's part
        Map<String, Integer> questionPartCache = new HashMap<>();
        for (UserAnswer a : answers) {
            int part = questionPartCache.computeIfAbsent(a.getQuestionId(), qId ->
                    questionRepository.findById(qId).map(Question::getPart).orElse(0));
            if (part == 0) continue;

            result.compute(part, (k, stats) -> {
                if (stats == null) {
                    return AnswerHistoryResponse.PartStats.builder()
                            .part(part)
                            .total(1)
                            .correct(a.isCorrect() ? 1 : 0)
                            .accuracy(a.isCorrect() ? 100.0 : 0.0)
                            .build();
                }
                long newTotal = stats.getTotal() + 1;
                long newCorrect = stats.getCorrect() + (a.isCorrect() ? 1 : 0);
                stats.setTotal(newTotal);
                stats.setCorrect(newCorrect);
                stats.setAccuracy(Math.round((double) newCorrect / newTotal * 10000.0) / 100.0);
                return stats;
            });
        }
        return result;
    }
}
