package com.zest.toeic.shared.event;

public record XpAwardedEvent(
        String userId,
        int xpAmount,
        String source,
        String sourceId,
        String description
) {}
