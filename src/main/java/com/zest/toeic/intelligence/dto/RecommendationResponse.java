package com.zest.toeic.intelligence.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    private List<Recommendation> recommendations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Recommendation {
        private String type; // PRACTICE, REVIEW, TEST
        private String description;
        private int targetPart;
        private String priority; // HIGH, MEDIUM, LOW
        private String estimatedImpact;
    }
}
