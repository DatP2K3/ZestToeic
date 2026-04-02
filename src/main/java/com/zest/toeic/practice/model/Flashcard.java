package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "flashcards")
@CompoundIndex(name = "user_review_idx", def = "{'userId': 1, 'nextReviewAt': 1}")
public class Flashcard extends BaseDocument {

    private String userId;
    private String front;           // Question / term
    private String back;            // Answer / definition
    private List<String> tags;
    private Integer part;           // TOEIC Part (optional)
    private String difficulty;

    // SM-2 state
    @Builder.Default
    private double easeFactor = 2.5;
    @Builder.Default
    private int interval = 0;       // days
    @Builder.Default
    private int repetitions = 0;
    @Builder.Default
    private String status = "LEARNING"; // LEARNING, REVIEW, MASTERED

    private Instant nextReviewAt;
    private Instant lastReviewedAt;
}
