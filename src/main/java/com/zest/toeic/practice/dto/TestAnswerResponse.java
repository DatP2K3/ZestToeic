package com.zest.toeic.practice.dto;

import lombok.Builder;

@Builder
public record TestAnswerResponse(
        boolean correct,
        String correctAnswer,
        int answeredCount,
        int totalQuestions,
        int xpEarned
) {}
