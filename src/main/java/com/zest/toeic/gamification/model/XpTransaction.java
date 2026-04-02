package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "xp_transactions")
@CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
public class XpTransaction extends BaseDocument {

    private String userId;
    private int amount;
    private String source;      // ANSWER_CORRECT, ANSWER_WRONG, TEST_COMPLETE, FLASHCARD_REVIEW, STREAK_BONUS
    private String sourceId;    // questionId, testId, etc.
    private String description;
    private long totalXpAfter;  // running total snapshot
}
