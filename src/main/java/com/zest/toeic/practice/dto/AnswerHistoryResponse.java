package com.zest.toeic.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class AnswerHistoryResponse {
    private long totalAnswers;
    private long correctCount;
    private double accuracy;
    private Map<Integer, PartStats> partStats;
    private List<AnswerDetail> recentAnswers;

    @Data
    @Builder
    @AllArgsConstructor
    public static class PartStats {
        private int part;
        private long total;
        private long correct;
        private double accuracy;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class AnswerDetail {
        private String answerId;
        private String questionId;
        private int part;
        private String selectedOption;
        private String correctAnswer;
        private boolean correct;
        private int timeTaken;
        private String answeredAt;
    }
}
