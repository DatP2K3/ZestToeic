package com.zest.toeic.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProgressTrendResponse {

    private int weeksIncluded;
    private List<WeeklyData> trend;

    @Data
    @Builder
    @AllArgsConstructor
    public static class WeeklyData {
        private String weekStart;       // ISO date
        private long totalAnswered;
        private long correctCount;
        private double accuracy;
        private double averageTimeTaken;
        private Double deltaAccuracy;   // vs previous week (null for first)
    }
}
