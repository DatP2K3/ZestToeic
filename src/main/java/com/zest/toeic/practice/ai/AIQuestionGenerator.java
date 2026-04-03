package com.zest.toeic.practice.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.practice.dto.GenerateQuestionsResponse;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import com.zest.toeic.practice.scraper.QuestionValidator;
import com.zest.toeic.shared.ai.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AIQuestionGenerator {

    private static final Logger log = LoggerFactory.getLogger(AIQuestionGenerator.class);
    private static final int MAX_PER_REQUEST = 20;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final GeminiClient geminiClient;
    private final QuestionRepository questionRepository;
    private final QuestionValidator questionValidator;

    public AIQuestionGenerator(GeminiClient geminiClient,
                               QuestionRepository questionRepository,
                               QuestionValidator questionValidator) {
        this.geminiClient = geminiClient;
        this.questionRepository = questionRepository;
        this.questionValidator = questionValidator;
    }

    public GenerateQuestionsResponse generate(int part, String difficulty, int count) {
        count = Math.min(count, MAX_PER_REQUEST);
        String prompt = buildPrompt(part, difficulty, count);

        log.info("Generating {} Part {} questions (difficulty: {})", count, part, difficulty);

        String rawResponse;
        try {
            rawResponse = geminiClient.ask(prompt);
        } catch (Exception e) {
            log.error("Gemini API error during generation: {}", e.getMessage());
            return new GenerateQuestionsResponse(0, 0, 0, List.of("Gemini API error: " + e.getMessage()));
        }

        List<Question> parsed;
        try {
            parsed = parseQuestions(rawResponse, part, difficulty);
        } catch (Exception e) {
            log.error("Parse error during generation: {}", e.getMessage());
            return new GenerateQuestionsResponse(0, 0, 0, List.of("Parse error: " + e.getMessage()));
        }
        List<String> errors = new ArrayList<>();
        int saved = 0;

        for (Question q : parsed) {
            if (!questionValidator.isValid(q)) {
                errors.add("Invalid structure: " + (q.getContent() != null ? q.getContent().substring(0, Math.min(50, q.getContent().length())) : "null"));
                continue;
            }
            if (questionValidator.isDuplicate(q, questionRepository)) {
                errors.add("Duplicate: " + q.getContent().substring(0, Math.min(50, q.getContent().length())));
                continue;
            }
            questionRepository.save(q);
            saved++;
        }

        log.info("AI Generation: {} parsed, {} saved, {} errors", parsed.size(), saved, errors.size());

        return new GenerateQuestionsResponse(
                parsed.size(),
                saved,
                parsed.size() - saved - errors.size(),
                errors
        );
    }

    private String buildPrompt(int part, String difficulty, int count) {
        String partDescription = switch (part) {
            case 5 -> "Part 5: Incomplete Sentences — fill-in-the-blank grammar/vocabulary questions";
            case 6 -> "Part 6: Text Completion — read short passage, fill in 4 blanks with correct words/phrases";
            case 7 -> "Part 7: Reading Comprehension — read passage and answer questions";
            default -> "Part " + part + " practice questions";
        };

        return """
                Generate exactly %d TOEIC %s for practice.
                Difficulty level: %s
                
                IMPORTANT: Respond ONLY with a JSON array. No markdown, no code blocks, no explanation.
                
                Each question must have this EXACT JSON structure:
                [
                  {
                    "content": "The question text with _____ blank if applicable",
                    "options": [
                      {"label": "A", "text": "option text"},
                      {"label": "B", "text": "option text"},
                      {"label": "C", "text": "option text"},
                      {"label": "D", "text": "option text"}
                    ],
                    "correctAnswer": "B",
                    "explanation": "Explanation in Vietnamese why this is correct",
                    "category": "GRAMMAR or VOCABULARY or READING"
                  }
                ]
                
                Requirements:
                - All questions must be realistic TOEIC-style
                - Options must be plausible distractors
                - Explanations must be in Vietnamese
                - Category must be one of: GRAMMAR, VOCABULARY, READING
                - For %s difficulty: %s
                """.formatted(
                count, partDescription, difficulty,
                difficulty,
                switch (difficulty.toUpperCase()) {
                    case "EASY" -> "basic grammar, common vocabulary, short simple sentences";
                    case "HARD" -> "complex grammar (subjunctive, inversion), advanced vocabulary, nuanced meaning";
                    default -> "intermediate grammar, business vocabulary, standard sentence structure";
                }
        );
    }

    @SuppressWarnings("unchecked")
    private List<Question> parseQuestions(String rawJson, int part, String difficulty) {
        List<Question> result = new ArrayList<>();

        try {
            // Clean response — remove markdown code blocks if present
            String cleaned = rawJson.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            List<Map<String, Object>> items = objectMapper.readValue(cleaned, new TypeReference<>() {});

            for (Map<String, Object> item : items) {
                try {
                    List<Map<String, String>> optionMaps = (List<Map<String, String>>) item.get("options");
                    List<Question.QuestionOption> options = optionMaps.stream()
                            .map(o -> Question.QuestionOption.builder()
                                    .label(o.get("label"))
                                    .text(o.get("text"))
                                    .build())
                            .toList();

                    Question q = Question.builder()
                            .part(part)
                            .difficulty(QuestionDifficulty.valueOf(difficulty.toUpperCase()))
                            .category((String) item.get("category"))
                            .content((String) item.get("content"))
                            .options(options)
                            .correctAnswer((String) item.get("correctAnswer"))
                            .explanation((String) item.get("explanation"))
                            .source("ai_generated")
                            .aiConfidence(0.90)
                            .status(QuestionStatus.PUBLISHED)
                            .build();

                    result.add(q);
                } catch (Exception e) {
                    log.warn("Failed to parse question item: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response for question generation", e);
            throw new RuntimeException("Không thể xử lý dữ liệu từ AI", e);
        }

        return result;
    }
}
