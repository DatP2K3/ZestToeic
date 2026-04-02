package com.zest.toeic.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MistakePatternResponse {

    private long totalMistakes;
    private List<MistakePattern> patterns;

    @Data
    @Builder
    @AllArgsConstructor
    public static class MistakePattern {
        private String category;
        private int part;
        private long mistakeCount;
        private double percentage;
        private List<String> exampleQuestionIds;
    }
}
