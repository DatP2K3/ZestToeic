package com.zest.toeic.practice.service;

import com.zest.toeic.auth.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.shared.event.XpAwardedEvent;
import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.dto.TestAnswerResponse;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.TestSessionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import com.zest.toeic.shared.model.enums.SessionStatus;
import com.zest.toeic.shared.model.enums.TestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private TestSessionRepository testSessionRepository;
    @Mock private UserAnswerRepository userAnswerRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private TestScoringService testScoringService;
    private QuestionSelectorService questionSelectorService;
    private AdaptiveDifficultyService adaptiveDifficultyService;
    private TestService testService;

    private Question mockQuestion;
    private TestSession mockSession;

    @BeforeEach
    void setUp() {
        testScoringService = new TestScoringService(userRepository, questionRepository);
        questionSelectorService = new QuestionSelectorService(questionRepository);
        adaptiveDifficultyService = new AdaptiveDifficultyService();
        testService = new TestService(questionRepository, testSessionRepository, userAnswerRepository, eventPublisher, testScoringService, questionSelectorService, adaptiveDifficultyService);

        mockQuestion = Question.builder()
                .part(5)
                .category("GRAMMAR")
                .difficulty(QuestionDifficulty.MEDIUM)
                .status(QuestionStatus.PUBLISHED)
                .correctAnswer("B")
                .build();
        mockQuestion.setId("q1");

        mockSession = TestSession.builder()
                .userId("user1")
                .type(TestType.MINI)
                .status(SessionStatus.IN_PROGRESS)
                .questionIds(List.of("q1"))
                .answers(new ArrayList<>())
                .totalQuestions(1)
                .startedAt(Instant.now())
                .build();
        mockSession.setId("s1");
    }

    @Test
    void startPlacementTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.PLACEMENT, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(mockSession));
        assertThrows(BadRequestException.class, () -> testService.startPlacementTest("user1"));
    }

    @Test
    void startPlacementTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.PLACEMENT, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(testSessionRepository.findByUserIdAndTypeAndStatusAndCreatedAtAfter(
                eq("user1"), eq(TestType.PLACEMENT), eq(SessionStatus.COMPLETED), any()))
                .thenReturn(List.of());
        when(questionRepository.findByPartAndDifficultyAndStatus(anyInt(), any(QuestionDifficulty.class), any(QuestionStatus.class)))
                .thenReturn(List.of(mockQuestion));
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        TestSession result = testService.startPlacementTest("user1");

        assertEquals(TestType.PLACEMENT, result.getType());
        assertEquals(SessionStatus.IN_PROGRESS, result.getStatus());
        assertEquals(25, result.getTotalQuestions());
        assertNotNull(result.getQuestionIds());
    }

    @Test
    void startMockTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MOCK, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(questionRepository.findByPartAndStatus(anyInt(), any(QuestionStatus.class)))
                .thenReturn(List.of(mockQuestion));
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        TestSession result = testService.startMockTest("user1");

        assertEquals(TestType.MOCK, result.getType());
        assertEquals(SessionStatus.IN_PROGRESS, result.getStatus());
        assertEquals(120 * 60, result.getTimeLimitSeconds());
    }

    @Test
    void startMiniTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MINI, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(questionRepository.findByPartAndDifficultyAndStatus(eq(5), eq(QuestionDifficulty.EASY), any(QuestionStatus.class)))
                .thenReturn(List.of(mockQuestion, mockQuestion));
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setPart(5);
        req.setDifficulty("EASY");
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);

        assertEquals(TestType.MINI, result.getType());
        assertEquals(SessionStatus.IN_PROGRESS, result.getStatus());
        assertEquals(2, result.getTotalQuestions());
    }

    @Test
    void submitTestAnswer_CorrectAnswer_ReturnsTrue() {
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("b");
        req.setTimeTaken(10);

        TestAnswerResponse result = testService.submitTestAnswer("user1", "s1", req);

        assertTrue(result.correct());
        assertEquals("B", result.correctAnswer());
        assertEquals(1, result.answeredCount());
        assertEquals(10, result.xpEarned());

        verify(userAnswerRepository).save(any());
        verify(testSessionRepository).save(mockSession);
    }

    @Test
    void submitTestAnswer_AlreadyAnswered_ThrowsException() {
        mockSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");

        assertThrows(BadRequestException.class, () -> testService.submitTestAnswer("user1", "s1", req));
    }

    @Test
    void submitTestAnswer_NotInProgress_ThrowsException() {
        mockSession.setStatus(SessionStatus.COMPLETED);
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        assertThrows(BadRequestException.class, () -> testService.submitTestAnswer("user1", "s1", new TestAnswerRequest()));
    }

    @Test
    void submitTestAnswer_QuestionNotFound_ThrowsException() {
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q2");
        when(questionRepository.findById("q2")).thenReturn(Optional.empty());
        assertThrows(com.zest.toeic.shared.exception.ResourceNotFoundException.class,
                () -> testService.submitTestAnswer("user1", "s1", req));
    }

    @Test
    void completeTest_AlreadyCompleted_ThrowsException() {
        mockSession.setStatus(SessionStatus.COMPLETED);
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        assertThrows(BadRequestException.class, () -> testService.completeTest("user1", "s1"));
    }

    @Test
    void startPlacementTest_CooldownActive_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.PLACEMENT, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        TestSession completed = TestSession.builder().build();
        completed.setCreatedAt(Instant.now());
        when(testSessionRepository.findByUserIdAndTypeAndStatusAndCreatedAtAfter(
                eq("user1"), eq(TestType.PLACEMENT), eq(SessionStatus.COMPLETED), any()))
                .thenReturn(List.of(completed));
        assertThrows(BadRequestException.class, () -> testService.startPlacementTest("user1"));
    }

    @Test
    void startMockTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MOCK, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(mockSession));
        assertThrows(BadRequestException.class, () -> testService.startMockTest("user1"));
    }

    @Test
    void startMiniTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MINI, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(mockSession));
        assertThrows(BadRequestException.class, () -> testService.startMiniTest("user1", new StartTestRequest()));
    }

    @Test
    void completeTest_Success() {
        mockSession.getAnswers().add(TestSession.TestAnswer.builder()
                .questionId("q1").correct(true).selectedOption("B").build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        when(questionRepository.findAllById(any())).thenReturn(List.of(mockQuestion));

        TestResult result = testService.completeTest("user1", "s1");

        assertEquals(SessionStatus.COMPLETED, mockSession.getStatus());
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectCount());
        assertEquals(100.0, result.getAccuracy());

        verify(testSessionRepository).save(mockSession);
        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
    }

    @Test
    void startMiniTest_WithPartOnly() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MINI, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(questionRepository.findByPartAndStatus(eq(5), any(QuestionStatus.class)))
                .thenReturn(List.of(mockQuestion));
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setPart(5);
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);
        assertEquals(TestType.MINI, result.getType());
        assertEquals(1, result.getTotalQuestions());
    }

    @Test
    void startMiniTest_NoFilters() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", TestType.MINI, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(questionRepository.findByStatus(QuestionStatus.PUBLISHED)).thenReturn(List.of(mockQuestion));
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);
        assertEquals(TestType.MINI, result.getType());
        assertEquals(1, result.getTotalQuestions());
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Hard() {
        TestSession s = TestSession.builder()
                .type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).totalQuestions(25).build();
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(true).build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(s));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1"); req.setSelectedOption("b"); req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals(QuestionDifficulty.HARD, s.getCurrentDifficulty());
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Easy() {
        TestSession s = TestSession.builder()
                .type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).totalQuestions(25).build();
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(false).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(false).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(false).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(s));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1"); req.setSelectedOption("b"); req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals(QuestionDifficulty.EASY, s.getCurrentDifficulty());
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Medium() {
        TestSession s = TestSession.builder()
                .type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).totalQuestions(25).build();
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(false).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(s));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1"); req.setSelectedOption("b"); req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals(QuestionDifficulty.MEDIUM, s.getCurrentDifficulty());
    }

    @Test
    void completeTest_Placement_LevelAssignment() {
        TestSession s = TestSession.builder()
                .type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now().minusSeconds(60)).build();
        s.setId("s1");
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.HARD).correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.MEDIUM).correct(true).build());
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.EASY).correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(s));
        when(questionRepository.findAllById(any())).thenReturn(List.of(mockQuestion));
        com.zest.toeic.auth.model.User user = new com.zest.toeic.auth.model.User();
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        TestResult result = testService.completeTest("user1", "s1");

        assertNotNull(result.getEstimatedScore());
        assertNotNull(result.getLevel());
        assertEquals(SessionStatus.COMPLETED, s.getStatus());
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
    }

    @Test
    void completeTest_Mock() {
        TestSession s = TestSession.builder()
                .type(TestType.MOCK).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now().minusSeconds(60)).build();
        s.setId("s1");
        s.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(null).correct(true).build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(s));
        when(questionRepository.findAllById(any())).thenReturn(List.of(mockQuestion));

        TestResult result = testService.completeTest("user1", "s1");

        assertNotNull(result.getEstimatedScore());
        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
    }

    @Test
    void completeTest_Placement_LevelAssignment_VariousScores() {
        com.zest.toeic.auth.model.User user = new com.zest.toeic.auth.model.User();

        // NOVICE (< 400)
        TestSession s1 = TestSession.builder().type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s1.setId("s1");
        when(testSessionRepository.findByIdAndUserId("s1", "u1")).thenReturn(Optional.of(s1));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        assertEquals("NOVICE", testService.completeTest("u1", "s1").getLevel());

        // INTERMEDIATE (400-595)
        TestSession s2 = TestSession.builder().type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s2.setId("s2");
        s2.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.MEDIUM).correct(true).build());
        s2.getAnswers().add(TestSession.TestAnswer.builder().questionId("q2").difficulty(QuestionDifficulty.MEDIUM).correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s2", "u2")).thenReturn(Optional.of(s2));
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));
        when(questionRepository.findAllById(any())).thenReturn(List.of(mockQuestion));
        assertEquals("INTERMEDIATE", testService.completeTest("u2", "s2").getLevel());

        // ADVANCED (600-795)
        TestSession s3 = TestSession.builder().type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s3.setId("s3");
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.MEDIUM).correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q2").difficulty(QuestionDifficulty.MEDIUM).correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q3").difficulty(QuestionDifficulty.MEDIUM).correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q4").difficulty(QuestionDifficulty.MEDIUM).correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s3", "u3")).thenReturn(Optional.of(s3));
        when(userRepository.findById("u3")).thenReturn(Optional.of(user));
        assertEquals("ADVANCED", testService.completeTest("u3", "s3").getLevel());

        // EXPERT (800+)
        TestSession s4 = TestSession.builder().type(TestType.PLACEMENT).status(SessionStatus.IN_PROGRESS)
                .answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s4.setId("s4");
        s4.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty(QuestionDifficulty.HARD).correct(true).build());
        when(testSessionRepository.findByIdAndUserId("s4", "u4")).thenReturn(Optional.of(s4));
        when(userRepository.findById("u4")).thenReturn(Optional.of(user));
        assertEquals("EXPERT", testService.completeTest("u4", "s4").getLevel());
    }
}
