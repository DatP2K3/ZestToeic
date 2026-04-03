package com.zest.toeic.shared.ai;

import com.zest.toeic.practice.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final GeminiKeyManager keyManager;

    public GeminiClient(GeminiKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isAvailable() {
        return keyManager != null && keyManager.hasAvailableKeys();
    }

    /**
     * Generic prompt → response.
     */
    public String ask(String prompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API key not configured");
        }
        return callGemini(prompt, 2048);
    }

    /**
     * Explain a TOEIC question in Vietnamese.
     */
    public String explain(Question question, String userAnswer) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API key not configured");
        }
        String prompt = buildExplainPrompt(question, userAnswer);
        return callGemini(prompt, 1024);
    }

    private String callGemini(String prompt, int maxTokens) {
        String url = baseUrl + "/models/" + model + ":generateContent";

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", maxTokens
                )
        );

        String apiKey = keyManager.getNextAvailableKey();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new org.springframework.core.ParameterizedTypeReference<>() {}
            );

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        keyManager.recordSuccess(apiKey);
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            keyManager.recordFailure(apiKey);
            return null;
        }
    }

    private String buildExplainPrompt(Question question, String userAnswer) {
        String optionsText = question.getOptions().stream()
                .map(o -> o.getLabel() + ". " + o.getText())
                .collect(java.util.stream.Collectors.joining("\n"));

        return """
                Bạn là giáo viên TOEIC chuyên nghiệp. Hãy giải thích câu hỏi TOEIC sau bằng tiếng Việt.
                
                **Câu hỏi (Part %d):**
                %s
                
                **Đáp án:**
                %s
                
                **Đáp án đúng:** %s
                **Người dùng chọn:** %s
                
                Hãy giải thích:
                1. Tại sao đáp án %s đúng
                2. Tại sao các đáp án còn lại sai
                3. Mẹo ghi nhớ (nếu có)
                
                Viết ngắn gọn, dễ hiểu, tối đa 200 từ.
                """.formatted(
                question.getPart(),
                question.getContent(),
                optionsText,
                question.getCorrectAnswer(),
                userAnswer,
                question.getCorrectAnswer()
        );
    }
}
