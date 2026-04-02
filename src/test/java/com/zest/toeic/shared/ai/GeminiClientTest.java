package com.zest.toeic.shared.ai;

import com.zest.toeic.practice.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeminiClient geminiClient;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        // Inject mock RestTemplate using ReflectionTestUtils since it's hardcoded
        ReflectionTestUtils.setField(geminiClient, "restTemplate", restTemplate);

        mockQuestion = Question.builder()
                .part(5)
                .content("Test Content")
                .options(List.of(
                        Question.QuestionOption.builder().label("A").text("opt1").build(),
                        Question.QuestionOption.builder().label("B").text("opt2").build()
                ))
                .correctAnswer("A")
                .build();
    }

    @Test
    void isAvailable_ApiKeyNotConfigured_ReturnsFalse() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "");
        assertFalse(geminiClient.isAvailable());

        ReflectionTestUtils.setField(geminiClient, "apiKey", null);
        assertFalse(geminiClient.isAvailable());
    }

    @Test
    void isAvailable_ApiKeyConfigured_ReturnsTrue() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "test-key");
        assertTrue(geminiClient.isAvailable());
    }

    @Test
    void ask_ApiKeyNotConfigured_ThrowsException() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "");
        assertThrows(IllegalStateException.class, () -> geminiClient.ask("prompt"));
    }

    @Test
    void explain_ApiKeyNotConfigured_ThrowsException() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "");
        assertThrows(IllegalStateException.class, () -> geminiClient.explain(mockQuestion, "B"));
    }

    @Test
    void ask_Success_ReturnsText() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "test-key");
        ReflectionTestUtils.setField(geminiClient, "model", "test-model");
        ReflectionTestUtils.setField(geminiClient, "baseUrl", "https://test");

        Map<String, Object> mockBody = Map.of(
                "candidates", List.of(Map.of(
                        "content", Map.of(
                                "parts", List.of(Map.of("text", "AI Response"))
                        )
                ))
        );
        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(mockBody);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        String result = geminiClient.ask("Hello");

        assertEquals("AI Response", result);
    }

    @Test
    void ask_RestTemplateThrowsException_ReturnsNull() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "test-key");
        ReflectionTestUtils.setField(geminiClient, "model", "test-model");
        ReflectionTestUtils.setField(geminiClient, "baseUrl", "https://test");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        String result = geminiClient.ask("Hello");

        assertNull(result);
    }

    @Test
    void explain_Success_BuildsPromptAndCallsOpenAI() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "test-key");
        ReflectionTestUtils.setField(geminiClient, "model", "test-model");
        ReflectionTestUtils.setField(geminiClient, "baseUrl", "https://test");

        Map<String, Object> mockBody = Map.of(
                "candidates", List.of(Map.of(
                        "content", Map.of(
                                "parts", List.of(Map.of("text", "Explanation details"))
                        )
                ))
        );
        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(mockBody);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        String result = geminiClient.explain(mockQuestion, "B");

        assertEquals("Explanation details", result);
    }
}
