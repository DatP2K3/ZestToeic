package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "test_sessions")
public class TestSession extends BaseDocument {

    @Indexed
    private String userId;

    private String type; // PLACEMENT, MOCK, MINI

    @Builder.Default
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, ABANDONED

    private TestConfig config;

    private List<String> questionIds;

    @Builder.Default
    private List<TestAnswer> answers = new ArrayList<>();

    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private Integer estimatedScore; // TOEIC 10-990
    private int timeLimitSeconds;
    private int timeSpentSeconds;

    private Instant startedAt;
    private Instant completedAt;

    // Adaptive state for PLACEMENT
    private String currentDifficulty;
    private int currentQuestionIndex;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestConfig {
        private Integer part;       // null = all parts
        private String difficulty;  // null = mixed
        private int questionCount;
        private int timeLimitMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestAnswer {
        private String questionId;
        private String selectedOption;
        private boolean correct;
        private int timeTaken;
        private String difficulty;
    }
}
