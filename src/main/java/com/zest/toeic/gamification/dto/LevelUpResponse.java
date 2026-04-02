package com.zest.toeic.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LevelUpResponse {
    private int oldLevel;
    private int newLevel;
    private String newLevelTitle;
    private long totalXp;
    private boolean leveledUp;
}
