package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.UserStreak;
import com.zest.toeic.gamification.repository.UserStreakRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class StreakService {

    private static final Logger log = LoggerFactory.getLogger(StreakService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final List<Integer> MILESTONE_THRESHOLDS = List.of(7, 30, 100);

    private final UserStreakRepository userStreakRepository;
    private final GamificationService gamificationService;

    public StreakService(UserStreakRepository userStreakRepository, GamificationService gamificationService) {
        this.userStreakRepository = userStreakRepository;
        this.gamificationService = gamificationService;
    }

    public UserStreak recordActivity(String userId) {
        LocalDate today = LocalDate.now(ZONE);

        UserStreak streak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.builder().userId(userId).build());

        if (today.equals(streak.getLastActiveDate())) {
            return streak; // Already recorded today
        }

        LocalDate yesterday = today.minusDays(1);

        if (yesterday.equals(streak.getLastActiveDate())) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1); // Reset, start new streak
        }

        streak.setLastActiveDate(today);

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        // Check milestones
        for (int milestone : MILESTONE_THRESHOLDS) {
            if (streak.getCurrentStreak() == milestone && !streak.getMilestones().contains(milestone)) {
                streak.getMilestones().add(milestone);
                gamificationService.awardXp(userId, 50, "STREAK_MILESTONE",
                        null, "Streak milestone: " + milestone + " days 🔥");
                log.info("User {} reached streak milestone: {} days", userId, milestone);
            }
        }

        return userStreakRepository.save(streak);
    }

    public UserStreak getStreakInfo(String userId) {
        return userStreakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.builder().userId(userId).currentStreak(0).longestStreak(0).build());
    }

    public int resetInactiveStreaks() {
        LocalDate yesterday = LocalDate.now(ZONE).minusDays(1);
        List<UserStreak> inactive = userStreakRepository.findByLastActiveDateBefore(yesterday);

        int resetCount = 0;
        for (UserStreak streak : inactive) {
            if (streak.getCurrentStreak() > 0) {
                streak.setCurrentStreak(0);
                userStreakRepository.save(streak);
                resetCount++;
            }
        }

        log.info("🔄 Streak reset: {} users reset", resetCount);
        return resetCount;
    }
}
