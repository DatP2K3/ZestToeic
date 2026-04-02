package com.zest.toeic.practice.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.gamification.service.GamificationService;
import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.TestSessionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    private static final int PLACEMENT_QUESTION_COUNT = 25;
    private static final int MOCK_QUESTION_COUNT = 200;
    private static final int MOCK_TIME_LIMIT_MIN = 120;
    private static final int PLACEMENT_RETAKE_DAYS = 7;

    // TOEIC structure: Part → question count for Mock Test
    private static final Map<Integer, Integer> TOEIC_STRUCTURE = Map.of(
            1, 6, 2, 25, 3, 39, 4, 30, 5, 30, 6, 16, 7, 54
    );

    // Part distribution for Placement Test (25 questions)
    private static final Map<Integer, Integer> PLACEMENT_DISTRIBUTION = Map.of(
            1, 2, 2, 2, 3, 3, 4, 3, 5, 5, 6, 3, 7, 7
    );

    private final QuestionRepository questionRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    public TestService(QuestionRepository questionRepository,
                       TestSessionRepository testSessionRepository,
                       UserAnswerRepository userAnswerRepository,
                       UserRepository userRepository,
                       GamificationService gamificationService) {
        this.questionRepository = questionRepository;
        this.testSessionRepository = testSessionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    // ========== PLACEMENT TEST ==========

    public TestSession startPlacementTest(String userId) {
        // Check for existing in-progress placement
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, "PLACEMENT", "IN_PROGRESS")
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Placement Test chưa hoàn thành"); });

        // Check cooldown (7 days between retakes)
        List<TestSession> recentPlacements = testSessionRepository
                .findByUserIdAndTypeAndStatusAndCreatedAtAfter(
                        userId, "PLACEMENT", "COMPLETED",
                        Instant.now().minus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS));
        if (!recentPlacements.isEmpty()) {
            throw new BadRequestException("Placement Test chỉ được làm lại sau 7 ngày. Lần tiếp: "
                    + recentPlacements.get(0).getCreatedAt().plus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS));
        }

        List<String> questionIds = selectAdaptiveQuestions("MEDIUM");

        TestSession session = TestSession.builder()
                .userId(userId)
                .type("PLACEMENT")
                .status("IN_PROGRESS")
                .config(TestSession.TestConfig.builder()
                        .questionCount(PLACEMENT_QUESTION_COUNT)
                        .timeLimitMinutes(0)
                        .build())
                .questionIds(questionIds)
                .answers(new ArrayList<>())
                .totalQuestions(PLACEMENT_QUESTION_COUNT)
                .timeLimitSeconds(0)
                .currentDifficulty("MEDIUM")
                .currentQuestionIndex(0)
                .startedAt(Instant.now())
                .build();

        return testSessionRepository.save(session);
    }

    // ========== MOCK TEST ==========

    public TestSession startMockTest(String userId) {
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, "MOCK", "IN_PROGRESS")
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Mock Test chưa hoàn thành"); });

        List<String> questionIds = selectMockQuestions();

        TestSession session = TestSession.builder()
                .userId(userId)
                .type("MOCK")
                .status("IN_PROGRESS")
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
        testSessionRepository.findByUserIdAndTypeAndStatus(userId, "MINI", "IN_PROGRESS")
                .ifPresent(s -> { throw new BadRequestException("Bạn đang có Mini Test chưa hoàn thành"); });

        List<Question> available;
        if (request.getPart() != null && request.getDifficulty() != null) {
            available = questionRepository.findByPartAndDifficultyAndStatus(
                    request.getPart(), request.getDifficulty().toUpperCase(), "PUBLISHED");
        } else if (request.getPart() != null) {
            available = questionRepository.findByPartAndStatus(request.getPart(), "PUBLISHED");
        } else {
            available = questionRepository.findAll().stream()
                    .filter(q -> "PUBLISHED".equals(q.getStatus()))
                    .toList();
        }

        int count = Math.min(request.getQuestionCount(), available.size());
        var shuffled = new ArrayList<>(available);
        Collections.shuffle(shuffled);
        List<String> questionIds = shuffled.stream().limit(count).map(Question::getId).toList();

        TestSession session = TestSession.builder()
                .userId(userId)
                .type("MINI")
                .status("IN_PROGRESS")
                .config(TestSession.TestConfig.builder()
                        .part(request.getPart())
                        .difficulty(request.getDifficulty())
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

    public Map<String, Object> submitTestAnswer(String userId, String testId, TestAnswerRequest request) {
        TestSession session = testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BadRequestException("Test đã kết thúc");
        }

        // Check duplicate answer
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

        // Adaptive difficulty for PLACEMENT
        if ("PLACEMENT".equals(session.getType())) {
            updateAdaptiveDifficulty(session);
        }

        // Also save to user_answers collection for global history
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("correct", isCorrect);
        result.put("correctAnswer", question.getCorrectAnswer());
        result.put("answeredCount", session.getAnswers().size());
        result.put("totalQuestions", session.getTotalQuestions());
        result.put("xpEarned", isCorrect ? 10 : 0);
        return result;
    }

    // ========== COMPLETE TEST ==========

    public TestResult completeTest(String userId, String testId) {
        TestSession session = testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));

        if ("COMPLETED".equals(session.getStatus())) {
            throw new BadRequestException("Test đã hoàn thành trước đó");
        }

        int correctCount = (int) session.getAnswers().stream().filter(TestSession.TestAnswer::isCorrect).count();
        int totalAnswered = session.getAnswers().size();
        double accuracy = totalAnswered > 0 ? (double) correctCount / totalAnswered * 100 : 0;
        int timeSpent = (int) java.time.Duration.between(session.getStartedAt(), Instant.now()).getSeconds();

        session.setStatus("COMPLETED");
        session.setCorrectCount(correctCount);
        session.setAccuracy(Math.round(accuracy * 100.0) / 100.0);
        session.setTimeSpentSeconds(timeSpent);
        session.setCompletedAt(Instant.now());

        // Calculate TOEIC score for PLACEMENT and MOCK
        Integer estimatedScore = null;
        String level = null;
        if ("PLACEMENT".equals(session.getType()) || "MOCK".equals(session.getType())) {
            estimatedScore = calculateToeicScore(session);
            session.setEstimatedScore(estimatedScore);

            if ("PLACEMENT".equals(session.getType())) {
                level = assignLevel(estimatedScore);
                updateUserLevel(userId, estimatedScore, level);
            }
        }

        testSessionRepository.save(session);

        // Award XP for completing test
        int xpReward = switch (session.getType()) {
            case "PLACEMENT" -> 100;
            case "MOCK" -> 200;
            default -> 50; // MINI
        };
        gamificationService.awardXp(userId, xpReward, "TEST_COMPLETE", testId,
                session.getType() + " test completed — score: " + (estimatedScore != null ? estimatedScore : "N/A"));

        // Build per-part scores
        Map<Integer, TestResult.PartScore> partScores = buildPartScores(session);

        // Calculate retake info for PLACEMENT
        boolean canRetake = false;
        String nextRetakeAt = null;
        if ("PLACEMENT".equals(session.getType())) {
            nextRetakeAt = Instant.now().plus(PLACEMENT_RETAKE_DAYS, ChronoUnit.DAYS).toString();
        }

        return TestResult.builder()
                .testSessionId(session.getId())
                .type(session.getType())
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

    public TestSession getTestSession(String userId, String testId) {
        return testSessionRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Test session not found"));
    }

    public Page<TestSession> getTestHistory(String userId, String type, int page, int size) {
        return testSessionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                userId, type, PageRequest.of(page, size));
    }

    // ========== PRIVATE METHODS ==========

    private List<String> selectAdaptiveQuestions(String initialDifficulty) {
        List<String> selectedIds = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : PLACEMENT_DISTRIBUTION.entrySet()) {
            int part = entry.getKey();
            int count = entry.getValue();

            // Try requested difficulty first, fallback to any difficulty
            List<Question> questions = questionRepository
                    .findByPartAndDifficultyAndStatus(part, initialDifficulty, "PUBLISHED");

            if (questions.size() < count) {
                questions = questionRepository.findByPartAndStatus(part, "PUBLISHED");
            }

            var shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);
            shuffled.stream().limit(count).map(Question::getId).forEach(selectedIds::add);
        }

        Collections.shuffle(selectedIds);
        return selectedIds;
    }

    private List<String> selectMockQuestions() {
        List<String> selectedIds = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : TOEIC_STRUCTURE.entrySet()) {
            int part = entry.getKey();
            int count = entry.getValue();

            List<Question> questions = questionRepository.findByPartAndStatus(part, "PUBLISHED");
            var shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);
            shuffled.stream().limit(count).map(Question::getId).forEach(selectedIds::add);
        }

        return selectedIds;
    }

    private void updateAdaptiveDifficulty(TestSession session) {
        List<TestSession.TestAnswer> answers = session.getAnswers();
        int answered = answers.size();

        if (answered >= 5) {
            long correct = answers.stream().filter(TestSession.TestAnswer::isCorrect).count();
            double rate = (double) correct / answered;

            if (rate >= 0.75) {
                session.setCurrentDifficulty("HARD");
            } else if (rate <= 0.40) {
                session.setCurrentDifficulty("EASY");
            } else {
                session.setCurrentDifficulty("MEDIUM");
            }
        }
    }

    private int calculateToeicScore(TestSession session) {
        // Weighted scoring: EASY=1, MEDIUM=2, HARD=3
        double totalWeight = 0;
        double earnedWeight = 0;

        for (TestSession.TestAnswer answer : session.getAnswers()) {
            double weight = switch (answer.getDifficulty() != null ? answer.getDifficulty() : "MEDIUM") {
                case "EASY" -> 1.0;
                case "HARD" -> 3.0;
                default -> 2.0;
            };
            totalWeight += weight;
            if (answer.isCorrect()) {
                earnedWeight += weight;
            }
        }

        if (totalWeight == 0) return 10;

        double ratio = earnedWeight / totalWeight;
        // TOEIC scale: 10-990, map ratio to this range
        int score = (int) Math.round(10 + ratio * 980);
        // Round to nearest 5
        return (score / 5) * 5;
    }

    private String assignLevel(int score) {
        if (score >= 800) return "EXPERT";
        if (score >= 600) return "ADVANCED";
        if (score >= 400) return "INTERMEDIATE";
        return "NOVICE";
    }

    private void updateUserLevel(String userId, int score, String level) {
        userRepository.findById(userId).ifPresent(user -> {
            int numericLevel = switch (level) {
                case "EXPERT" -> 4;
                case "ADVANCED" -> 3;
                case "INTERMEDIATE" -> 2;
                default -> 1;
            };
            user.setLevel(numericLevel);
            userRepository.save(user);
            log.info("User {} placement: score={}, level={} ({})", userId, score, level, numericLevel);
        });
    }

    private Map<Integer, TestResult.PartScore> buildPartScores(TestSession session) {
        Map<Integer, int[]> partData = new LinkedHashMap<>(); // part -> [total, correct]

        for (TestSession.TestAnswer answer : session.getAnswers()) {
            questionRepository.findById(answer.getQuestionId()).ifPresent(q -> {
                partData.computeIfAbsent(q.getPart(), k -> new int[]{0, 0});
                int[] data = partData.get(q.getPart());
                data[0]++;
                if (answer.isCorrect()) data[1]++;
            });
        }

        return partData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> TestResult.PartScore.builder()
                                .total(e.getValue()[0])
                                .correct(e.getValue()[1])
                                .accuracy(Math.round((double) e.getValue()[1] / e.getValue()[0] * 10000.0) / 100.0)
                                .build(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
