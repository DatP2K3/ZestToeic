package com.zest.toeic.gamification.scheduler;

import com.zest.toeic.gamification.service.WeeklyGoalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyGoalResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyGoalResetScheduler.class);

    private final WeeklyGoalService weeklyGoalService;

    public WeeklyGoalResetScheduler(WeeklyGoalService weeklyGoalService) {
        this.weeklyGoalService = weeklyGoalService;
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Ho_Chi_Minh")
    public void expireOldWeeklyGoals() {
        log.info("📅 Starting weekly goal expiration check...");
        int expired = weeklyGoalService.expireOldGoals();
        log.info("✅ Weekly goal check done: {} goals expired", expired);
    }
}
