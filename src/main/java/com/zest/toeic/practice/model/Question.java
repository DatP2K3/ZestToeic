package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "questions")
public class Question extends BaseDocument {

    private int part; // 1-7
    private String difficulty; // EASY, MEDIUM, HARD
    private String category; // GRAMMAR, VOCABULARY, LISTENING, READING
    private String content;
    private String audioUrl;
    private String imageUrl;
    private List<QuestionOption> options;
    private String correctAnswer; // A, B, C, D
    private String explanation;

    @Builder.Default
    private String status = "PUBLISHED";

    private String source; // study4, ai_generated, manual
    private Double aiConfidence;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOption {
        private String label; // A, B, C, D
        private String text;
    }
}
