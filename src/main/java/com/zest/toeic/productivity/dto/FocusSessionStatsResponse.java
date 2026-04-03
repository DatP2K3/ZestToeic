package com.zest.toeic.productivity.dto;

public record FocusSessionStatsResponse(
        long totalSessions,
        long totalDurationMinutes
) {}
