package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.SessionStatus;
import com.zest.toeic.shared.model.enums.TestType;
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

    private TestType type;

    @Builder.Default
    private SessionStatus status = SessionStatus.IN_PROGRESS;

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
    private QuestionDifficulty currentDifficulty;
    private int currentQuestionIndex;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestConfig {
        private Integer part;       // null = all parts
        private QuestionDifficulty difficulty;  // null = mixed
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
        private QuestionDifficulty difficulty;
    }
}
