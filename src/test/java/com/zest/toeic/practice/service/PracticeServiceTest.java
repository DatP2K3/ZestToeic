package com.zest.toeic.practice.service;

import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.shared.event.XpAwardedEvent;
import com.zest.toeic.practice.dto.AnswerHistoryResponse;
import com.zest.toeic.practice.dto.AnswerResult;
import com.zest.toeic.practice.dto.SubmitAnswerRequest;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserAnswerRepository userAnswerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PracticeService practiceService;

    private Question mockQuestion;
    private UserAnswer mockUserAnswer;

    @BeforeEach
    void setUp() {
        mockQuestion = Question.builder()
                .part(5)
                .category("GRAMMAR")
                .difficulty(QuestionDifficulty.MEDIUM)
                .status(QuestionStatus.PUBLISHED)
                .correctAnswer("B")
                .build();
        mockQuestion.setId("q1");

        mockUserAnswer = UserAnswer.builder()
                .userId("user1")
                .questionId("q1")
                .selectedOption("B")
                .correct(true)
                .timeTaken(10)
                .build();
        mockUserAnswer.setId("a1");
        mockUserAnswer.setCreatedAt(Instant.now());
    }

    @Test
    void getRandomQuestions_WithPartAndDifficulty_ReturnsQuestions() {
        when(questionRepository.findByPartAndDifficultyAndStatus(5, QuestionDifficulty.MEDIUM, QuestionStatus.PUBLISHED))
                .thenReturn(List.of(mockQuestion, mockQuestion));

        List<Question> result = practiceService.getRandomQuestions(5, "MEDIUM", 5);

        assertEquals(2, result.size());
        verify(questionRepository).findByPartAndDifficultyAndStatus(5, QuestionDifficulty.MEDIUM, QuestionStatus.PUBLISHED);
    }

    @Test
    void getRandomQuestions_WithOnlyPart_ReturnsQuestions() {
        when(questionRepository.findByPartAndStatus(5, QuestionStatus.PUBLISHED))
                .thenReturn(List.of(mockQuestion));

        List<Question> result = practiceService.getRandomQuestions(5, null, 5);

        assertEquals(1, result.size());
        verify(questionRepository).findByPartAndStatus(5, QuestionStatus.PUBLISHED);
    }

    @Test
    void getRandomQuestions_NoFilters_ReturnsAllPublishedQuestions() {
        Question draftQuestion = Question.builder().status(QuestionStatus.DRAFT).build();
        draftQuestion.setId("q2");
        when(questionRepository.findByStatus(QuestionStatus.PUBLISHED)).thenReturn(List.of(mockQuestion));

        List<Question> result = practiceService.getRandomQuestions(null, null, 5);

        assertEquals(1, result.size());
        assertEquals("q1", result.get(0).getId());
    }

    @Test
    void submitAnswer_CorrectAnswer_Awards10XpAndSaves() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setQuestionId("q1");
        request.setSelectedOption("B"); // Correct
        request.setTimeTaken(15);

        AnswerResult result = practiceService.submitAnswer("user1", request);

        assertTrue(result.isCorrect());
        assertEquals("B", result.getCorrectAnswer());
        assertEquals(10, result.getXpEarned());

        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
        
        ArgumentCaptor<UserAnswer> captor = ArgumentCaptor.forClass(UserAnswer.class);
        verify(userAnswerRepository).save(captor.capture());
        assertTrue(captor.getValue().isCorrect());
    }

    @Test
    void submitAnswer_WrongAnswer_Awards2XpAndSaves() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setQuestionId("q1");
        request.setSelectedOption("A"); // Wrong
        request.setTimeTaken(15);

        AnswerResult result = practiceService.submitAnswer("user1", request);

        assertFalse(result.isCorrect());
        assertEquals(2, result.getXpEarned());

        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
        
        ArgumentCaptor<UserAnswer> captor = ArgumentCaptor.forClass(UserAnswer.class);
        verify(userAnswerRepository).save(captor.capture());
        assertFalse(captor.getValue().isCorrect());
    }

    @Test
    void submitAnswer_QuestionNotFound_ThrowsException() {
        when(questionRepository.findById("q1")).thenReturn(Optional.empty());
        
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setQuestionId("q1");

        assertThrows(ResourceNotFoundException.class, () -> practiceService.submitAnswer("user1", request));
    }

    @Test
    void getQuestionById_Found_ReturnsQuestion() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        Question result = practiceService.getQuestionById("q1");
        assertEquals("q1", result.getId());
    }

    @Test
    void getAnswerHistory_CalculatesCorrectly() {
        when(userAnswerRepository.countByUserId("user1")).thenReturn(10L);
        when(userAnswerRepository.countByUserIdAndCorrect("user1", true)).thenReturn(8L);
        
        Page<UserAnswer> page = new PageImpl<>(List.of(mockUserAnswer));
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc(eq("user1"), any(PageRequest.class)))
                .thenReturn(page);
                
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(mockUserAnswer));
                
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        AnswerHistoryResponse response = practiceService.getAnswerHistory("user1", 0, 10);

        assertEquals(10L, response.getTotalAnswers());
        assertEquals(8L, response.getCorrectCount());
        assertEquals(80.0, response.getAccuracy());
        
        assertNotNull(response.getPartStats());
        assertTrue(response.getPartStats().containsKey(5));
        assertEquals(1, response.getPartStats().get(5).getTotal());
        assertEquals(1, response.getPartStats().get(5).getCorrect());
        assertEquals(100.0, response.getPartStats().get(5).getAccuracy());
        
        assertEquals(1, response.getRecentAnswers().size());
        assertEquals("q1", response.getRecentAnswers().get(0).getQuestionId());
    }
    @Test
    void getAnswerHistory_EmptyAnswers_ReturnsZeros() {
        when(userAnswerRepository.countByUserId("user1")).thenReturn(0L);
        when(userAnswerRepository.countByUserIdAndCorrect("user1", true)).thenReturn(0L);
        
        Page<UserAnswer> emptyPage = new PageImpl<>(List.of());
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc(eq("user1"), any(PageRequest.class)))
                .thenReturn(emptyPage);
                
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of());

        AnswerHistoryResponse response = practiceService.getAnswerHistory("user1", 0, 10);

        assertEquals(0L, response.getTotalAnswers());
        assertEquals(0.0, response.getAccuracy());
        assertTrue(response.getRecentAnswers().isEmpty());
    }

    @Test
    void getAnswerHistory_WithNullQuestionAndNullCreatedAt_HandlesGracefully() {
        UserAnswer noDateOrQ = UserAnswer.builder().questionId("qMissing").correct(false).build();
        noDateOrQ.setId("a2");
        // createdAt is null
        
        when(userAnswerRepository.countByUserId("user1")).thenReturn(1L);
        when(userAnswerRepository.countByUserIdAndCorrect("user1", true)).thenReturn(0L);
        
        Page<UserAnswer> page = new PageImpl<>(List.of(noDateOrQ));
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc(eq("user1"), any(PageRequest.class)))
                .thenReturn(page);
                
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(noDateOrQ));
                
        when(questionRepository.findById("qMissing")).thenReturn(Optional.empty());

        AnswerHistoryResponse response = practiceService.getAnswerHistory("user1", 0, 10);

        // partStats shouldn't contain this because question is not found (part=0)
        assertTrue(response.getPartStats().isEmpty());
        
        // recentAnswers should have defaults
        assertEquals(1, response.getRecentAnswers().size());
        AnswerHistoryResponse.AnswerDetail detail = response.getRecentAnswers().get(0);
        assertEquals(0, detail.getPart());
        assertEquals("", detail.getCorrectAnswer());
        assertEquals("", detail.getAnsweredAt());
    }

    @Test
    void getAnswerHistory_MultipleAnswersSamePart_UpdatesStats() {
        // Prepare two answers for the same question (part 5)
        UserAnswer ans1 = UserAnswer.builder().questionId("q1").correct(true).build();
        UserAnswer ans2 = UserAnswer.builder().questionId("q1").correct(false).build();

        when(userAnswerRepository.countByUserId("user1")).thenReturn(2L);
        when(userAnswerRepository.countByUserIdAndCorrect("user1", true)).thenReturn(1L);
        
        Page<UserAnswer> page = new PageImpl<>(List.of(ans1, ans2));
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc(eq("user1"), any(PageRequest.class)))
                .thenReturn(page);
                
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(ans1, ans2)); // simulate both answers
                
        // Both answers hit the same question which has part=5
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        AnswerHistoryResponse response = practiceService.getAnswerHistory("user1", 0, 10);

        assertEquals(1, response.getPartStats().size()); // Only part 5
        AnswerHistoryResponse.PartStats stats = response.getPartStats().get(5);
        assertEquals(2, stats.getTotal()); // 2 answers
        assertEquals(1, stats.getCorrect()); // 1 correct
        assertEquals(50.0, stats.getAccuracy()); // 50%
    }
}
