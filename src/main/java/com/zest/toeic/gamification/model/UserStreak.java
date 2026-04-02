package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_streaks")
public class UserStreak extends BaseDocument {

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private int currentStreak = 0;

    @Builder.Default
    private int longestStreak = 0;

    private LocalDate lastActiveDate;

    @Builder.Default
    private List<Integer> milestones = new ArrayList<>(); // e.g. [7, 30, 100]
}
