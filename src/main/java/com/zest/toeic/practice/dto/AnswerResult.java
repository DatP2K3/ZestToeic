package com.zest.toeic.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AnswerResult {
    private String questionId;
    private boolean correct;
    private String correctAnswer;
    private String selectedOption;
    private int xpEarned;
}
