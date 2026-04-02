package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "daily_quests")
@CompoundIndex(name = "idx_user_date", def = "{'userId': 1, 'date': 1}", unique = true)
public class DailyQuest extends BaseDocument {

    private String userId;
    private LocalDate date;

    @Builder.Default
    private List<Quest> quests = new ArrayList<>();

    @Builder.Default
    private boolean allCompleted = false;

    @Builder.Default
    private boolean bonusClaimed = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quest {
        private String type;       // PRACTICE_QUESTIONS, REVIEW_FLASHCARDS, COMPLETE_TEST
        private String description;
        private int target;
        private int progress;
        private boolean completed;
        private boolean claimed;
        private int xpReward;
    }
}
