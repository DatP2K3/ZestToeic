package com.zest.toeic.gamification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {
    private int rank;
    private String userId;
    private String displayName;
    private long totalXp;
    private int level;
}
