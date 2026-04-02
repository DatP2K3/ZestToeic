package com.zest.toeic.shared.ai;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.scraper.QuestionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIQuestionGeneratorTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionValidator questionValidator;

    @InjectMocks
    private AIQuestionGenerator aiQuestionGenerator;

    @BeforeEach
    void setUp() {
    }

    @Test
    void generate_GeminiError_ReturnsErrorMap() {
        when(geminiClient.ask(anyString())).thenThrow(new RuntimeException("API quota exceeded"));

        Map<String, Object> result = aiQuestionGenerator.generate(5, "EASY", 1);

        assertEquals(0, result.get("generated"));
        assertEquals(0, result.get("saved"));
        assertTrue(((java.util.List<?>) result.get("errors")).get(0).toString().contains("Gemini API error"));
    }

    @Test
    void generate_InvalidJson_ReturnsEmpty() {
        when(geminiClient.ask(anyString())).thenReturn("This is not json");

        Map<String, Object> result = aiQuestionGenerator.generate(5, "EASY", 1);

        assertEquals(0, result.get("generated"));
        assertEquals(0, result.get("saved"));
    }

    @Test
    void generate_SuccessfulParsing_ValidatesAndSaves() {
        String json = """
                ```json
                [
                  {
                    "content": "This is a question _____.",
                    "options": [
                      {"label": "A", "text": "foo"},
                      {"label": "B", "text": "bar"}
                    ],
                    "correctAnswer": "A",
                    "explanation": "Because foo.",
                    "category": "GRAMMAR"
                  }
                ]
                ```
                """;

        when(geminiClient.ask(anyString())).thenReturn(json);
        when(questionValidator.isValid(any(Question.class))).thenReturn(true);
        when(questionValidator.isDuplicate(any(Question.class), eq(questionRepository))).thenReturn(false);

        Map<String, Object> result = aiQuestionGenerator.generate(5, "HARD", 1);

        assertEquals(1, result.get("generated"));
        assertEquals(1, result.get("saved"));
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void generate_Duplicate_DoesNotSave() {
        String json = """
                [
                  {
                    "content": "This is a duplicate question.",
                    "options": [{"label":"A", "text":"val"}]
                  }
                ]
                """;

        when(geminiClient.ask(anyString())).thenReturn(json);
        when(questionValidator.isValid(any(Question.class))).thenReturn(true);
        when(questionValidator.isDuplicate(any(Question.class), eq(questionRepository))).thenReturn(true);

        Map<String, Object> result = aiQuestionGenerator.generate(6, "MEDIUM", 1);

        assertEquals(1, result.get("generated"));
        assertEquals(0, result.get("saved"));
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void generate_InvalidStructure_DoesNotSave() {
        String json = """
                [
                  {
                    "content": "Invalid Structure question.",
                    "options": []
                  }
                ]
                """;

        when(geminiClient.ask(anyString())).thenReturn(json);
        when(questionValidator.isValid(any(Question.class))).thenReturn(false);

        Map<String, Object> result = aiQuestionGenerator.generate(7, "EASY", 1);

        assertEquals(1, result.get("generated"));
        assertEquals(0, result.get("saved"));
        verify(questionRepository, never()).save(any(Question.class));
    }
}
