package com.zest.toeic.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class WeaknessResponse {

    private int totalWeaknesses;
    private List<Weakness> weaknesses;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Weakness {
        private int part;
        private String category;
        private double accuracy;
        private long totalAnswered;
        private String severity;  // CRITICAL, HIGH, MEDIUM, LOW
    }
}
