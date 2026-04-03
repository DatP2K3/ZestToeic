package com.zest.toeic.practice.service;

import com.zest.toeic.shared.event.XpAwardedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.dto.TestAnswerResponse;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.TestSessionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import com.zest.toeic.shared.model.enums.SessionStatus;
import com.zest.toeic.shared.model.enums.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class TestService {

    private static final int PLACEMENT_QUESTION_COUNT = 25;
    private static final int MOCK_QUESTION_COUNT = 200;
    private static final int MOCK_TIME_LIMIT_MIN = 120;
    private static final int PLACEMENT_RETAKE_DAYS = 7;

    private final QuestionRepository questionRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    // Injected newly extracted services
    private final TestScoringService testScoringService;
    private final QuestionSelectorService questionSelectorService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;

    public TestService(QuestionRepository questionRepository,
                       TestSessionRepository testSessionRepository,
                       UserAnswerRepository userAnswerRepository,
                       ApplicationEventPublisher eventPublisher,
                       TestScoringService testScoringService,
                       QuestionSelectorService questionSelectorService,
                       AdaptiveDifficultyService adaptiveDifficultyService) {
        this.questionRepository = questionRepository;
        this.testSessionRepository = testSessionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.eventPublisher = eventPublisher;
        this.testScoringService = testScoringService;
        this.questionSelectorService = questionSelectorService;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
    }

    // ========== PLACEMENT TEST ==========

    public TestSession startPlacementTest(String userId) {
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, TestType.PLACEMENT, SessionStatus.IN_PROGRESS)
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Placement Test chưa hoàn thành"); });

        List<TestSession> recentPlacements = testSessionRepository
                .findByUserIdAndTypeAndStatusAndCreatedAtAfter(
                        userId, TestType.PLACEMENT, SessionStatus.COMPLETED,
                        Instant.now().minus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS));
        if (!recentPlacements.isEmpty()) {
            throw new BadRequestException("Placement Test chỉ được làm lại sau 7 ngày. Lần tiếp: "
                    + recentPlacements.get(0).getCreatedAt().plus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS));
        }

        List<String> questionIds = questionSelectorService.selectAdaptiveQuestions(QuestionDifficulty.MEDIUM);

        TestSession session = TestSession.builder()
                .userId(userId)
                .type(TestType.PLACEMENT)
                .status(SessionStatus.IN_PROGRESS)
                .config(TestSession.TestConfig.builder()
                        .questionCount(PLACEMENT_QUESTION_COUNT)
                        .timeLimitMinutes(0)
                        .build())
                .questionIds(questionIds)
                .answers(new ArrayList<>())
                .totalQuestions(PLACEMENT_QUESTION_COUNT)
                .timeLimitSeconds(0)
                .currentDifficulty(QuestionDifficulty.MEDIUM)
                .currentQuestionIndex(0)
                .startedAt(Instant.now())
                .build();

        return testSessionRepository.save(session);
    }

    // ========== MOCK TEST ==========

    public TestSession startMockTest(String userId) {
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, TestType.MOCK, SessionStatus.IN_PROGRESS)
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Mock Test chưa hoàn thành"); });

        List<String> questionIds = questionSelectorService.selectMockQuestions();

        TestSession session = TestSession.builder()
                .userId(userId)
                .type(TestType.MOCK)
                .status(SessionStatus.IN_PROGRESS)
                .config(TestSession.TestConfig.builder()
                        .questionCount(MOCK_QUESTION_COUNT)
                        .timeLimitMinutes(MOCK_TIME_LIMIT_MIN)
                        .build())
                .questionIds(questionIds)
                .answers(new ArrayList<>())
                .totalQuestions(questionIds.size())
                .timeLimitSeconds(MOCK_TIME_LIMIT_MIN * 60)
                .startedAt(Instant.now())
                .build();

        return testSessionRepository.save(session);
    }

    // ========== MINI TEST ==========

    public TestSession startMiniTest(String userId, StartTestRequest request) {
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, TestType.MINI, SessionStatus.IN_PROGRESS)
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Mini Test chưa hoàn thành"); });

        List<Question> available;
        if (request.getPart() != null && request.getDifficulty() != null) {
            available = questionRepository.findByPartAndDifficultyAndStatus(
                    request.getPart(), QuestionDifficulty.valueOf(request.getDifficulty().toUpperCase()), QuestionStatus.PUBLISHED);
        } else if (request.getPart() != null) {
            available = questionRepository.findByPartAndStatus(request.getPart(), QuestionStatus.PUBLISHED);
        } else {
            available = questionRepository.findByStatus(QuestionStatus.PUBLISHED);
        }

        int count = Math.min(request.getQuestionCount(), available.size());
        var shuffled = new ArrayList<>(available);
        Collections.shuffle(shuffled);
        List<String> questionIds = shuffled.stream().limit(count).map(Question::getId).toList();

        TestSession session = TestSession.builder()
                .userId(userId)
                .type(TestType.MINI)
                .status(SessionStatus.IN_PROGRESS)
                .config(TestSession.TestConfig.builder()
                        .part(request.getPart())
                        .difficulty(request.getDifficulty() != null ? QuestionDifficulty.valueOf(request.getDifficulty().toUpperCase()) : null)
                        .questionCount(count)
                        .timeLimitMinutes(request.getTimeLimitMinutes())
                        .build())
                .questionIds(questionIds)
                .answers(new ArrayList<>())
                .totalQuestions(count)
                .timeLimitSeconds(request.getTimeLimitMinutes() * 60)
                .startedAt(Instant.now())
                .build();

        return testSessionRepository.save(session);
    }

    // ========== SUBMIT ANSWER ==========

    public TestAnswerResponse submitTestAnswer(String userId, String testId, TestAnswerRequest request) {
        TestSession session = testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Test đã kết thúc");
        }

        boolean alreadyAnswered = session.getAnswers().stream()
                .anyMatch(a -> a.getQuestionId().equals(request.getQuestionId()));
        if (alreadyAnswered) {
            throw new BadRequestException("Câu hỏi này đã được trả lời");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(request.getSelectedOption());

        TestSession.TestAnswer answer = TestSession.TestAnswer.builder()
                .questionId(request.getQuestionId())
                .selectedOption(request.getSelectedOption().toUpperCase())
                .correct(isCorrect)
                .timeTaken(request.getTimeTaken())
                .difficulty(question.getDifficulty())
                .build();

        session.getAnswers().add(answer);
        session.setCurrentQuestionIndex(session.getAnswers().size());

        if (TestType.PLACEMENT.equals(session.getType())) {
            adaptiveDifficultyService.updateAdaptiveDifficulty(session);
        }

        UserAnswer userAnswer = UserAnswer.builder()
                .userId(userId)
                .questionId(request.getQuestionId())
                .testId(testId)
                .selectedOption(request.getSelectedOption().toUpperCase())
                .correct(isCorrect)
                .timeTaken(request.getTimeTaken())
                .build();
        userAnswerRepository.save(userAnswer);

        testSessionRepository.save(session);

        return TestAnswerResponse.builder()
                .correct(isCorrect)
                .correctAnswer(question.getCorrectAnswer())
                .answeredCount(session.getAnswers().size())
                .totalQuestions(session.getTotalQuestions())
                .xpEarned(isCorrect ? 10 : 0)
                .build();
    }

    // ========== COMPLETE TEST ==========

    public TestResult completeTest(String userId, String testId) {
        TestSession session = testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));

        if (SessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Test đã hoàn thành trước đó");
        }

        int correctCount = (int) session.getAnswers().stream().filter(TestSession.TestAnswer::isCorrect).count();
        int totalAnswered = session.getAnswers().size();
        double accuracy = totalAnswered > 0 ? (double) correctCount / totalAnswered * 100 : 0;
        int timeSpent = (int) java.time.Duration.between(session.getStartedAt(), Instant.now()).getSeconds();

        session.setStatus(SessionStatus.COMPLETED);
        session.setCorrectCount(correctCount);
        session.setAccuracy(Math.round(accuracy * 100.0) / 100.0);
        session.setTimeSpentSeconds(timeSpent);
        session.setCompletedAt(Instant.now());

        Integer estimatedScore = null;
        String level = null;
        if (TestType.PLACEMENT.equals(session.getType()) || TestType.MOCK.equals(session.getType())) {
            estimatedScore = testScoringService.calculateToeicScore(session);
            session.setEstimatedScore(estimatedScore);

            if (TestType.PLACEMENT.equals(session.getType())) {
                level = testScoringService.assignLevel(estimatedScore);
                testScoringService.updateUserLevel(userId, estimatedScore, level);
            }
        }

        testSessionRepository.save(session);

        int xpReward = switch (session.getType()) {
            case PLACEMENT -> 100;
            case MOCK -> 200;
            default -> 50;
        };
        eventPublisher.publishEvent(new XpAwardedEvent(userId, xpReward, "TEST_COMPLETE", testId, 
                "Score: " + (estimatedScore != null ? estimatedScore : 0) + ", duration: " + session.getTimeSpentSeconds()));

        Map<Integer, TestResult.PartScore> partScores = testScoringService.buildPartScores(session);

        boolean canRetake = false;
        String nextRetakeAt = null;
        if (TestType.PLACEMENT.equals(session.getType())) {
            nextRetakeAt = Instant.now().plus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS).toString();
        }

        return TestResult.builder()
                .testSessionId(session.getId())
                .type(session.getType().name())
                .totalQuestions(totalAnswered)
                .correctCount(correctCount)
                .accuracy(session.getAccuracy())
                .estimatedScore(estimatedScore)
                .level(level)
                .timeSpentSeconds(timeSpent)
                .partScores(partScores)
                .canRetake(canRetake)
                .nextRetakeAt(nextRetakeAt)
                .build();
    }

    // ========== GET TEST ==========

    @Transactional(readOnly = true)
    public TestSession getTestSession(String userId, String testId) {
        return testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));
    }

    @Transactional(readOnly = true)
    public Page<TestSession> getTestHistory(String userId, String type, int page, int size) {
        return testSessionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                userId, TestType.valueOf(type.toUpperCase()), PageRequest.of(page, size));
    }
}
