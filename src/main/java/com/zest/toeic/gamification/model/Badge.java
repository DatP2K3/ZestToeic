package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "badges")
public class Badge extends BaseDocument {

    private String name;
    private String description;
    private String iconUrl;
    private String criteria; // STREAK_7, STREAK_30, LEVEL_5, LEVEL_10, QUESTIONS_100, QUESTIONS_500, PERFECT_TEST
    private String category; // STREAK, LEVEL, PRACTICE, ACHIEVEMENT

    @Builder.Default
    private boolean active = true;
}
