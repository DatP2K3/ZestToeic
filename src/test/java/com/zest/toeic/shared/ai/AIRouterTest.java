package com.zest.toeic.shared.ai;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.ai.dto.AIExplanationResponse;
import com.zest.toeic.shared.ai.model.AIExplanation;
import com.zest.toeic.shared.ai.repository.AIExplanationRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIRouterTest {

    @Mock
    private AIExplanationRepository explanationRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private AIRouter aiRouter;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        mockQuestion = Question.builder().correctAnswer("A").explanation("Static explanation").build();
    }

    @Test
    void explain_FoundInCache_ReturnsCached() {
        AIExplanation cached = AIExplanation.builder().explanation("Cached explanation").provider("GEMINI").build();
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.of(cached));

        AIExplanationResponse res = aiRouter.explain("q1", "b");

        assertTrue(res.isCached());
        assertEquals("Cached explanation", res.getExplanation());
        assertEquals("GEMINI", res.getProvider());
    }

    @Test
    void explain_QuestionNotFound_ThrowsException() {
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.empty());
        when(questionRepository.findById("q1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> aiRouter.explain("q1", "b"));
    }

    @Test
    void explain_GeminiAvailableAndReturnsExplanation_CachesAndReturns() {
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.empty());
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        when(geminiClient.isAvailable()).thenReturn(true);
        when(geminiClient.explain(mockQuestion, "B")).thenReturn("Gemini explanation");

        AIExplanationResponse res = aiRouter.explain("q1", "b");

        assertFalse(res.isCached());
        assertEquals("Gemini explanation", res.getExplanation());
        assertEquals("GEMINI", res.getProvider());
        
        verify(explanationRepository, times(1)).save(any(AIExplanation.class));
    }

    @Test
    void explain_GeminiNotAvailable_ReturnsStaticExplanation() {
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.empty());
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        when(geminiClient.isAvailable()).thenReturn(false);

        AIExplanationResponse res = aiRouter.explain("q1", "b");

        assertFalse(res.isCached());
        assertEquals("Static explanation", res.getExplanation());
        assertEquals("STATIC", res.getProvider());
    }

    @Test
    void explain_NoGeminiAndNoStatic_ReturnsFallback() {
        mockQuestion.setExplanation(null);
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.empty());
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        when(geminiClient.isAvailable()).thenReturn(false);

        AIExplanationResponse res = aiRouter.explain("q1", "b");

        assertFalse(res.isCached());
        assertTrue(res.getExplanation().contains("Giải thích đang được cập nhật"));
        assertEquals("FALLBACK", res.getProvider());
    }

    @Test
    void explain_GeminiExplanationSaveFails_StillReturnsExplanation() {
        when(explanationRepository.findByQuestionIdAndUserAnswer("q1", "B")).thenReturn(Optional.empty());
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        
        when(geminiClient.isAvailable()).thenReturn(true);
        when(geminiClient.explain(mockQuestion, "B")).thenReturn("Exception test");
        
        when(explanationRepository.save(any(AIExplanation.class))).thenThrow(new RuntimeException("DB down"));

        AIExplanationResponse res = aiRouter.explain("q1", "b");

        assertEquals("Exception test", res.getExplanation());
    }
}
