package com.zest.toeic.shared.ai;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.ai.dto.AIExplanationResponse;
import com.zest.toeic.shared.ai.model.AIExplanation;
import com.zest.toeic.shared.ai.repository.AIExplanationRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AIRouter {

    private static final Logger log = LoggerFactory.getLogger(AIRouter.class);

    private final AIExplanationRepository explanationRepository;
    private final QuestionRepository questionRepository;
    private final GeminiClient geminiClient;

    public AIRouter(AIExplanationRepository explanationRepository,
                    QuestionRepository questionRepository,
                    GeminiClient geminiClient) {
        this.explanationRepository = explanationRepository;
        this.questionRepository = questionRepository;
        this.geminiClient = geminiClient;
    }

    public AIExplanationResponse explain(String questionId, String userAnswer) {
        String normalizedAnswer = userAnswer.toUpperCase();

        // 1. Check cache
        Optional<AIExplanation> cached = explanationRepository
                .findByQuestionIdAndUserAnswer(questionId, normalizedAnswer);

        if (cached.isPresent()) {
            log.debug("Cache hit for question={} answer={}", questionId, normalizedAnswer);
            return AIExplanationResponse.builder()
                    .questionId(questionId)
                    .userAnswer(normalizedAnswer)
                    .explanation(cached.get().getExplanation())
                    .provider(cached.get().getProvider())
                    .cached(true)
                    .build();
        }

        // 2. Load question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));

        // 3. Try Gemini
        if (geminiClient.isAvailable()) {
            String explanation = geminiClient.explain(question, normalizedAnswer);
            if (explanation != null) {
                saveToCache(questionId, normalizedAnswer, explanation, "GEMINI");
                return AIExplanationResponse.builder()
                        .questionId(questionId)
                        .userAnswer(normalizedAnswer)
                        .explanation(explanation)
                        .provider("GEMINI")
                        .cached(false)
                        .build();
            }
        }

        // 4. Fallback: static explanation from question
        String staticExplanation = question.getExplanation();
        if (staticExplanation != null && !staticExplanation.isBlank()) {
            return AIExplanationResponse.builder()
                    .questionId(questionId)
                    .userAnswer(normalizedAnswer)
                    .explanation(staticExplanation)
                    .provider("STATIC")
                    .cached(false)
                    .build();
        }

        // 5. Last resort
        return AIExplanationResponse.builder()
                .questionId(questionId)
                .userAnswer(normalizedAnswer)
                .explanation("Giải thích đang được cập nhật. Đáp án đúng là " + question.getCorrectAnswer() + ".")
                .provider("FALLBACK")
                .cached(false)
                .build();
    }

    private void saveToCache(String questionId, String userAnswer, String explanation, String provider) {
        try {
            AIExplanation entity = AIExplanation.builder()
                    .questionId(questionId)
                    .userAnswer(userAnswer)
                    .explanation(explanation)
                    .provider(provider)
                    .tokenCount(explanation.split("\\s+").length)
                    .build();
            explanationRepository.save(entity);
            log.info("Cached explanation for question={} answer={} provider={}", questionId, userAnswer, provider);
        } catch (Exception e) {
            log.warn("Failed to cache explanation: {}", e.getMessage());
        }
    }
}
