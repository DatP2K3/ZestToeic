package com.zest.toeic.practice.service;

import com.zest.toeic.practice.dto.AnswerResult;
import com.zest.toeic.practice.dto.SubmitAnswerRequest;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PracticeService {

    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;

    public PracticeService(QuestionRepository questionRepository,
                           UserAnswerRepository userAnswerRepository) {
        this.questionRepository = questionRepository;
        this.userAnswerRepository = userAnswerRepository;
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
        int xpEarned = isCorrect ? 10 : 0;

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
}
