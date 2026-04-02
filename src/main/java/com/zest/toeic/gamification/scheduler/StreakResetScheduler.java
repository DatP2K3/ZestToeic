package com.zest.toeic.gamification.scheduler;

import com.zest.toeic.gamification.service.StreakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StreakResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(StreakResetScheduler.class);

    private final StreakService streakService;

    public StreakResetScheduler(StreakService streakService) {
        this.streakService = streakService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void resetDailyStreaks() {
        log.info("🔄 Starting daily streak reset...");
        int resetCount = streakService.resetInactiveStreaks();
        log.info("✅ Daily streak reset done: {} users reset", resetCount);
    }
}
