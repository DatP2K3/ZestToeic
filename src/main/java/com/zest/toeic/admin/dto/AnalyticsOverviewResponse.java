package com.zest.toeic.admin.dto;

public record AnalyticsOverviewResponse(
        long totalUsers,
        long totalQuestions,
        String date,
        long totalTestSessions,
        long totalAnswers,
        long premiumUsers
) {}
