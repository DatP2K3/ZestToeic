package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.Badge;
import com.zest.toeic.gamification.model.UserBadge;
import com.zest.toeic.gamification.model.UserStreak;
import com.zest.toeic.gamification.repository.BadgeRepository;
import com.zest.toeic.gamification.repository.UserBadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeService.class);

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findByActiveTrue();
    }

    public List<UserBadge> getUserBadges(String userId) {
        return userBadgeRepository.findByUserId(userId);
    }

    public void checkAndAwardForStreak(String userId, UserStreak streak) {
        int currentStreak = streak.getCurrentStreak();
        if (currentStreak >= 7) tryAward(userId, "STREAK_7");
        if (currentStreak >= 30) tryAward(userId, "STREAK_30");
        if (currentStreak >= 100) tryAward(userId, "STREAK_100");
    }

    public void checkAndAwardForLevel(String userId, int level) {
        if (level >= 5) tryAward(userId, "LEVEL_5");
        if (level >= 10) tryAward(userId, "LEVEL_10");
        if (level >= 20) tryAward(userId, "LEVEL_20");
    }

    public void checkAndAwardForQuestions(String userId, long totalAnswered) {
        if (totalAnswered >= 100) tryAward(userId, "QUESTIONS_100");
        if (totalAnswered >= 500) tryAward(userId, "QUESTIONS_500");
        if (totalAnswered >= 1000) tryAward(userId, "QUESTIONS_1000");
    }

    public void checkAndAwardForPerfectTest(String userId) {
        tryAward(userId, "PERFECT_TEST");
    }

    private void tryAward(String userId, String criteria) {
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, criteria)) {
            return; // Already earned
        }
        badgeRepository.findByCriteria(criteria).ifPresent(badge -> {
            UserBadge userBadge = UserBadge.builder()
                    .userId(userId)
                    .badgeId(badge.getId())
                    .badgeName(badge.getName())
                    .earnedAt(LocalDateTime.now())
                    .build();
            userBadgeRepository.save(userBadge);
            log.info("🏅 Badge '{}' awarded to user {}", badge.getName(), userId);
        });
    }

    public void initDefaultBadges() {
        if (badgeRepository.count() > 0) return;

        List<Badge> defaults = List.of(
            Badge.builder().name("Streak 7 Ngày").description("Học liên tục 7 ngày").criteria("STREAK_7").category("STREAK").iconUrl("🔥").build(),
            Badge.builder().name("Streak 30 Ngày").description("Học liên tục 30 ngày").criteria("STREAK_30").category("STREAK").iconUrl("💪").build(),
            Badge.builder().name("Streak 100 Ngày").description("Học liên tục 100 ngày!").criteria("STREAK_100").category("STREAK").iconUrl("🏆").build(),
            Badge.builder().name("Level 5").description("Đạt Level 5").criteria("LEVEL_5").category("LEVEL").iconUrl("⭐").build(),
            Badge.builder().name("Level 10").description("Đạt Level 10").criteria("LEVEL_10").category("LEVEL").iconUrl("🌟").build(),
            Badge.builder().name("Level 20").description("Đạt Level 20").criteria("LEVEL_20").category("LEVEL").iconUrl("💫").build(),
            Badge.builder().name("100 Câu Hỏi").description("Trả lời 100 câu hỏi").criteria("QUESTIONS_100").category("PRACTICE").iconUrl("📝").build(),
            Badge.builder().name("500 Câu Hỏi").description("Trả lời 500 câu hỏi").criteria("QUESTIONS_500").category("PRACTICE").iconUrl("📚").build(),
            Badge.builder().name("1000 Câu Hỏi").description("Trả lời 1000 câu hỏi").criteria("QUESTIONS_1000").category("PRACTICE").iconUrl("🎓").build(),
            Badge.builder().name("Hoàn Hảo").description("Đạt điểm tuyệt đối trong 1 bài test").criteria("PERFECT_TEST").category("ACHIEVEMENT").iconUrl("💯").build()
        );
        badgeRepository.saveAll(defaults);
        log.info("✅ Initialized {} default badges", defaults.size());
    }
}
