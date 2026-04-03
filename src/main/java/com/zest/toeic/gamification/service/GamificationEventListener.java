package com.zest.toeic.gamification.service;

import com.zest.toeic.shared.event.XpAwardedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GamificationEventListener {

    private final GamificationService gamificationService;

    public GamificationEventListener(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @EventListener
    public void handleXpAwarded(XpAwardedEvent event) {
        gamificationService.awardXp(
                event.userId(),
                event.xpAmount(),
                event.source(),
                event.sourceId(),
                event.description()
        );
    }
}
