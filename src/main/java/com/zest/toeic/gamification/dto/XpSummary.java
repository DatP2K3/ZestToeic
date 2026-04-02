package com.zest.toeic.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class XpSummary {
    private long totalXp;
    private int level;
    private String levelTitle;
    private long xpForCurrentLevel;
    private long xpForNextLevel;
    private long xpProgress;         // XP earned within current level
    private double progressPercent;
    private int todayXp;
    private int dailyCap;
    private boolean capReached;
}
