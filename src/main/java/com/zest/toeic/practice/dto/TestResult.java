package com.zest.toeic.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class TestResult {
    private String testSessionId;
    private String type;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private Integer estimatedScore;   // TOEIC 10-990
    private String level;             // Placement only: NOVICE/INTERMEDIATE/ADVANCED/EXPERT
    private int timeSpentSeconds;
    private Map<Integer, PartScore> partScores;
    private boolean canRetake;        // Placement only
    private String nextRetakeAt;      // Placement only

    @Data
    @Builder
    @AllArgsConstructor
    public static class PartScore {
        private int total;
        private int correct;
        private double accuracy;
    }
}
