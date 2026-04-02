package com.zest.toeic.practice.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.gamification.service.GamificationService;
import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.TestSessionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TestSessionRepository testSessionRepository;

    @Mock
    private UserAnswerRepository userAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private TestService testService;

    private Question mockQuestion;
    private TestSession mockSession;

    @BeforeEach
    void setUp() {
        mockQuestion = Question.builder()
                .part(5)
                .category("GRAMMAR")
                .difficulty("MEDIUM")
                .status("PUBLISHED")
                .correctAnswer("B")
                .build();
        mockQuestion.setId("q1");

        mockSession = TestSession.builder()
                .userId("user1")
                .type("MINI")
                .status("IN_PROGRESS")
                .questionIds(List.of("q1"))
                .answers(new ArrayList<>())
                .totalQuestions(1)
                .startedAt(Instant.now())
                .build();
        mockSession.setId("s1");
    }

    @Test
    void startPlacementTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "PLACEMENT", "IN_PROGRESS"))
                .thenReturn(Optional.of(mockSession));

        assertThrows(BadRequestException.class, () -> testService.startPlacementTest("user1"));
    }

    @Test
    void startPlacementTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "PLACEMENT", "IN_PROGRESS"))
                .thenReturn(Optional.empty());
        when(testSessionRepository.findByUserIdAndTypeAndStatusAndCreatedAtAfter(eq("user1"), eq("PLACEMENT"), eq("COMPLETED"), any()))
                .thenReturn(List.of());
        when(questionRepository.findByPartAndDifficultyAndStatus(anyInt(), anyString(), anyString()))
                .thenReturn(List.of(mockQuestion));
        
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        TestSession result = testService.startPlacementTest("user1");

        assertEquals("PLACEMENT", result.getType());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals(25, result.getTotalQuestions());
        assertNotNull(result.getQuestionIds());
    }

    @Test
    void startMockTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MOCK", "IN_PROGRESS"))
                .thenReturn(Optional.empty());
        when(questionRepository.findByPartAndStatus(anyInt(), anyString()))
                .thenReturn(List.of(mockQuestion));
                
        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        TestSession result = testService.startMockTest("user1");

        assertEquals("MOCK", result.getType());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals(120 * 60, result.getTimeLimitSeconds());
    }

    @Test
    void startMiniTest_Success() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MINI", "IN_PROGRESS"))
                .thenReturn(Optional.empty());
                
        when(questionRepository.findByPartAndDifficultyAndStatus(5, "EASY", "PUBLISHED"))
                .thenReturn(List.of(mockQuestion, mockQuestion));

        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setPart(5);
        req.setDifficulty("EASY");
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);

        assertEquals("MINI", result.getType());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals(2, result.getTotalQuestions()); // min of requested vs available
    }

    @Test
    void submitTestAnswer_CorrectAnswer_ReturnsTrue() {
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("b"); // Case insensitive
        req.setTimeTaken(10);

        Map<String, Object> result = testService.submitTestAnswer("user1", "s1", req);

        assertTrue((Boolean) result.get("correct"));
        assertEquals("B", result.get("correctAnswer"));
        assertEquals(1, result.get("answeredCount"));
        assertEquals(10, result.get("xpEarned")); // 10 base xp

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
        mockSession.setStatus("COMPLETED");
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));

        assertThrows(BadRequestException.class, () -> testService.submitTestAnswer("user1", "s1", new TestAnswerRequest()));
    }

    @Test
    void submitTestAnswer_QuestionNotFound_ThrowsException() {
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q2");
        
        when(questionRepository.findById("q2")).thenReturn(Optional.empty());

        assertThrows(com.zest.toeic.shared.exception.ResourceNotFoundException.class, () -> testService.submitTestAnswer("user1", "s1", req));
    }

    @Test
    void completeTest_AlreadyCompleted_ThrowsException() {
        mockSession.setStatus("COMPLETED");
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));

        assertThrows(BadRequestException.class, () -> testService.completeTest("user1", "s1"));
    }

    @Test
    void startPlacementTest_CooldownActive_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "PLACEMENT", "IN_PROGRESS"))
                .thenReturn(Optional.empty());
                
        TestSession completed = TestSession.builder().build();
        completed.setCreatedAt(Instant.now());
        when(testSessionRepository.findByUserIdAndTypeAndStatusAndCreatedAtAfter(eq("user1"), eq("PLACEMENT"), eq("COMPLETED"), any()))
                .thenReturn(List.of(completed));

        assertThrows(BadRequestException.class, () -> testService.startPlacementTest("user1"));
    }

    @Test
    void startMockTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MOCK", "IN_PROGRESS"))
                .thenReturn(Optional.of(mockSession));

        assertThrows(BadRequestException.class, () -> testService.startMockTest("user1"));
    }

    @Test
    void startMiniTest_ExistingInProgress_ThrowsException() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MINI", "IN_PROGRESS"))
                .thenReturn(Optional.of(mockSession));

        assertThrows(BadRequestException.class, () -> testService.startMiniTest("user1", new StartTestRequest()));
    }

    @Test
    void completeTest_Success() {
        mockSession.getAnswers().add(TestSession.TestAnswer.builder()
                .questionId("q1").correct(true).selectedOption("B").build());
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion)); // for part score

        TestResult result = testService.completeTest("user1", "s1");

        assertEquals("COMPLETED", mockSession.getStatus());
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectCount());
        assertEquals(100.0, result.getAccuracy());

        verify(testSessionRepository).save(mockSession);
        verify(gamificationService).awardXp(eq("user1"), eq(50), eq("TEST_COMPLETE"), eq("s1"), anyString()); // MINI gives 50 xp
    }

    @Test
    void startMiniTest_WithPartOnly() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MINI", "IN_PROGRESS"))
                .thenReturn(Optional.empty());

        when(questionRepository.findByPartAndStatus(5, "PUBLISHED"))
                .thenReturn(List.of(mockQuestion));

        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setPart(5);
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);
        assertEquals("MINI", result.getType());
        assertEquals(1, result.getTotalQuestions());
    }

    @Test
    void startMiniTest_NoFilters() {
        when(testSessionRepository.findByUserIdAndTypeAndStatus("user1", "MINI", "IN_PROGRESS"))
                .thenReturn(Optional.empty());

        when(questionRepository.findAll())
                .thenReturn(List.of(mockQuestion));

        when(testSessionRepository.save(any(TestSession.class))).thenAnswer(i -> i.getArguments()[0]);

        StartTestRequest req = new StartTestRequest();
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        TestSession result = testService.startMiniTest("user1", req);
        assertEquals("MINI", result.getType());
        assertEquals(1, result.getTotalQuestions()); // mockQuestion has status PUBLISHED, but findAll is filtered
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Hard() {
        TestSession placementSession = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).totalQuestions(25).build();
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(true).build());
        
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(placementSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("b");
        req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals("HARD", placementSession.getCurrentDifficulty()); // 5/5 correct = 100% -> HARD
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Easy() {
        TestSession placementSession = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).totalQuestions(25).build();
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(false).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(false).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(false).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(false).build());

        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(placementSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("b"); // This one is correct -> 1/5 correct = 20% -> EASY
        req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals("EASY", placementSession.getCurrentDifficulty());
    }

    @Test
    void submitTestAnswer_Placement_AdaptiveDifficulty_Medium() {
        TestSession placementSession = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).totalQuestions(25).build();
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx1").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx2").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx3").correct(false).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("dx4").correct(false).build());
        
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(placementSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("b"); // Correct -> 3/5 correct = 60% -> MEDIUM
        req.setTimeTaken(10);

        testService.submitTestAnswer("user1", "s1", req);
        assertEquals("MEDIUM", placementSession.getCurrentDifficulty());
    }

    @Test
    void completeTest_Placement_LevelAssignment() {
        TestSession placementSession = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now().minusSeconds(60)).build();
        placementSession.setId("s1");
        // Give 1 hard question right, 1 medium right, and rest 0. Should get some score.
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("HARD").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("MEDIUM").correct(true).build());
        placementSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("EASY").correct(false).build());
        
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(placementSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        com.zest.toeic.auth.model.User user = new com.zest.toeic.auth.model.User();
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        TestResult result = testService.completeTest("user1", "s1");

        assertNotNull(result.getEstimatedScore());
        assertNotNull(result.getLevel());
        assertEquals("COMPLETED", placementSession.getStatus());
        verify(userRepository).save(user); // Level was updated
        verify(gamificationService).awardXp(eq("user1"), eq(100), anyString(), eq("s1"), anyString());
    }

    @Test
    void completeTest_Mock() {
        TestSession mockTestSession = TestSession.builder().type("MOCK").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now().minusSeconds(60)).build();
        mockTestSession.setId("s1");
        mockTestSession.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("NOT_SET").correct(true).build()); // Fallback to medium
        
        when(testSessionRepository.findByIdAndUserId("s1", "user1")).thenReturn(Optional.of(mockTestSession));
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        TestResult result = testService.completeTest("user1", "s1");

        assertNotNull(result.getEstimatedScore());
        verify(gamificationService).awardXp(eq("user1"), eq(200), anyString(), eq("s1"), anyString());
    }

    @Test
    void completeTest_Placement_LevelAssignment_VariousScores() {
        // Test NOVICE (< 400)
        TestSession s1 = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s1.setId("s1");
        // No answers -> score 10 -> NOVICE
        when(testSessionRepository.findByIdAndUserId("s1", "u1")).thenReturn(Optional.of(s1));
        
        com.zest.toeic.auth.model.User user = new com.zest.toeic.auth.model.User();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        
        TestResult r1 = testService.completeTest("u1", "s1");
        assertEquals("NOVICE", r1.getLevel());
        
        // Test INTERMEDIATE (400 - 595)
        TestSession s2 = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s2.setId("s2");
        // simulate exactly 50% accurate on MEDIUM (weight 2) => ratio 0.5 => score ~ 500
        s2.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("MEDIUM").correct(true).build());
        s2.getAnswers().add(TestSession.TestAnswer.builder().questionId("q2").difficulty("MEDIUM").correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s2", "u2")).thenReturn(Optional.of(s2));
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));
        when(questionRepository.findById(anyString())).thenReturn(Optional.of(mockQuestion));
        
        TestResult r2 = testService.completeTest("u2", "s2");
        assertEquals("INTERMEDIATE", r2.getLevel());

        // Test ADVANCED (600 - 795)
        TestSession s3 = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s3.setId("s3");
        // simulate 75% accuracy => ~ 745
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("MEDIUM").correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q2").difficulty("MEDIUM").correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q3").difficulty("MEDIUM").correct(true).build());
        s3.getAnswers().add(TestSession.TestAnswer.builder().questionId("q4").difficulty("MEDIUM").correct(false).build());
        when(testSessionRepository.findByIdAndUserId("s3", "u3")).thenReturn(Optional.of(s3));
        when(userRepository.findById("u3")).thenReturn(Optional.of(user));
        
        TestResult r3 = testService.completeTest("u3", "s3");
        assertEquals("ADVANCED", r3.getLevel());

        // Test EXPERT (800+)
        TestSession s4 = TestSession.builder().type("PLACEMENT").status("IN_PROGRESS").answers(new ArrayList<>()).startedAt(Instant.now()).build();
        s4.setId("s4");
        // simulate 100% accuracy => ~ 990
        s4.getAnswers().add(TestSession.TestAnswer.builder().questionId("q1").difficulty("HARD").correct(true).build());
        when(testSessionRepository.findByIdAndUserId("s4", "u4")).thenReturn(Optional.of(s4));
        when(userRepository.findById("u4")).thenReturn(Optional.of(user));
        
        TestResult r4 = testService.completeTest("u4", "s4");
        assertEquals("EXPERT", r4.getLevel());
    }
}
