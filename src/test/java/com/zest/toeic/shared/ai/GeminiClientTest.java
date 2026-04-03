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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GeminiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeminiKeyManager keyManager;

    @InjectMocks
    private GeminiClient geminiClient;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        // Inject mock RestTemplate using ReflectionTestUtils since it's hardcoded
        ReflectionTestUtils.setField(geminiClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(geminiClient, "model", "test-model");
        ReflectionTestUtils.setField(geminiClient, "baseUrl", "https://test");

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
    void isAvailable_NoAvailableKeys_ReturnsFalse() {
        when(keyManager.hasAvailableKeys()).thenReturn(false);
        assertFalse(geminiClient.isAvailable());
    }

    @Test
    void isAvailable_HasAvailableKeys_ReturnsTrue() {
        when(keyManager.hasAvailableKeys()).thenReturn(true);
        assertTrue(geminiClient.isAvailable());
    }

    @Test
    void ask_ApiKeyNotConfigured_ThrowsException() {
        when(keyManager.hasAvailableKeys()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> geminiClient.ask("prompt"));
    }

    @Test
    void explain_ApiKeyNotConfigured_ThrowsException() {
        when(keyManager.hasAvailableKeys()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> geminiClient.explain(mockQuestion, "B"));
    }

    @Test
    void ask_Success_ReturnsText() {
        when(keyManager.hasAvailableKeys()).thenReturn(true);
        when(keyManager.getNextAvailableKey()).thenReturn("test-key1");

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
        verify(keyManager).recordSuccess("test-key1");
        verify(keyManager, never()).recordFailure(anyString());
    }

    @Test
    void ask_RestTemplateThrowsException_ReturnsNull_AndRecordsFailure() {
        when(keyManager.hasAvailableKeys()).thenReturn(true);
        when(keyManager.getNextAvailableKey()).thenReturn("test-key2");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        String result = geminiClient.ask("Hello");

        assertNull(result);
        verify(keyManager).recordFailure("test-key2");
        verify(keyManager, never()).recordSuccess(anyString());
    }

    @Test
    void explain_Success_BuildsPromptAndCallsGemini() {
        when(keyManager.hasAvailableKeys()).thenReturn(true);
        when(keyManager.getNextAvailableKey()).thenReturn("test-key3");

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
        verify(keyManager).recordSuccess("test-key3");
    }
}
