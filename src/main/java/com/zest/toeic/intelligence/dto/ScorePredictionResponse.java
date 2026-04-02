package com.zest.toeic.intelligence.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScorePredictionResponse {

    private int predictedScore;
    private int listeningScore;
    private int readingScore;
    private double confidence;
    private List<String> improvementTips;
}
