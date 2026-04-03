package com.zest.toeic.gamification.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.gamification.dto.LevelUpResponse;
import com.zest.toeic.gamification.dto.XpSummary;
import com.zest.toeic.gamification.model.XpTransaction;
import com.zest.toeic.gamification.repository.XpTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);
    private static final int DAILY_XP_CAP = 500;
    private static final double SOFT_CAP_MULTIPLIER = 0.5;

    // Level thresholds: level → minXp
    private static final Map<Integer, Long> LEVEL_THRESHOLDS = Map.of(
            1, 0L,
            2, 500L,
            3, 1_500L,
            4, 3_500L,
            5, 7_000L,
            6, 12_000L,
            7, 20_000L,
            8, 35_000L
    );

    private static final Map<Integer, String> LEVEL_TITLES = Map.of(
            1, "Newcomer",
            2, "Learner",
            3, "Practitioner",
            4, "Scholar",
            5, "Expert",
            6, "Master",
            7, "Grandmaster",
            8, "Legend"
    );

    private final XpTransactionRepository xpRepo;
    private final UserRepository userRepository;

    public GamificationService(XpTransactionRepository xpRepo, UserRepository userRepository) {
        this.xpRepo = xpRepo;
        this.userRepository = userRepository;
    }

    /**
     * Award XP with soft cap (500/day normal, 50% after cap).
     * Returns LevelUpResponse if user leveled up.
     */
    public LevelUpResponse awardXp(String userId, int baseAmount, String source, String sourceId, String description) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        int oldLevel = user.getLevel();
        int todayXp = getTodayXp(userId);

        // Soft cap: full XP until 500, then 50%
        int actualAmount;
        if (todayXp >= DAILY_XP_CAP) {
            actualAmount = (int) Math.ceil(baseAmount * SOFT_CAP_MULTIPLIER);
        } else if (todayXp + baseAmount > DAILY_XP_CAP) {
            int fullPortion = DAILY_XP_CAP - todayXp;
            int reducedPortion = (int) Math.ceil((baseAmount - fullPortion) * SOFT_CAP_MULTIPLIER);
            actualAmount = fullPortion + reducedPortion;
        } else {
            actualAmount = baseAmount;
        }

        long newTotalXp = user.getTotalXp() + actualAmount;
        user.setTotalXp(newTotalXp);

        // Check level up
        int newLevel = calculateLevel(newTotalXp);
        if (newLevel > oldLevel) {
            user.setLevel(newLevel);
            log.info("🎉 User {} leveled up: {} → {} ({})", userId, oldLevel, newLevel, LEVEL_TITLES.get(newLevel));
        }

        userRepository.save(user);

        // Save transaction
        XpTransaction tx = XpTransaction.builder()
                .userId(userId)
                .amount(actualAmount)
                .source(source)
                .sourceId(sourceId)
                .description(description)
                .totalXpAfter(newTotalXp)
                .build();
        xpRepo.save(tx);

        return LevelUpResponse.builder()
                .oldLevel(oldLevel)
                .newLevel(newLevel)
                .newLevelTitle(LEVEL_TITLES.getOrDefault(newLevel, "Unknown"))
                .totalXp(newTotalXp)
                .leveledUp(newLevel > oldLevel)
                .build();
    }

    public XpSummary getXpSummary(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        int level = user.getLevel();
        long totalXp = user.getTotalXp();
        long currentLevelXp = LEVEL_THRESHOLDS.getOrDefault(level, 0L);
        long nextLevelXp = LEVEL_THRESHOLDS.getOrDefault(level + 1, currentLevelXp + 10_000);
        long xpProgress = totalXp - currentLevelXp;
        long xpRange = nextLevelXp - currentLevelXp;
        double progressPercent = xpRange > 0 ? Math.round((double) xpProgress / xpRange * 10000.0) / 100.0 : 100.0;

        int todayXp = getTodayXp(userId);

        return XpSummary.builder()
                .totalXp(totalXp)
                .level(level)
                .levelTitle(LEVEL_TITLES.getOrDefault(level, "Unknown"))
                .xpForCurrentLevel(currentLevelXp)
                .xpForNextLevel(nextLevelXp)
                .xpProgress(xpProgress)
                .progressPercent(progressPercent)
                .todayXp(todayXp)
                .dailyCap(DAILY_XP_CAP)
                .capReached(todayXp >= DAILY_XP_CAP)
                .build();
    }

    public Page<XpTransaction> getXpHistory(String userId, int page, int size) {
        return xpRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    private int calculateLevel(long totalXp) {
        int level = 1;
        for (Map.Entry<Integer, Long> entry : LEVEL_THRESHOLDS.entrySet()) {
            if (totalXp >= entry.getValue()) {
                level = Math.max(level, entry.getKey());
            }
        }
        return level;
    }

    private int getTodayXp(String userId) {
        Instant startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        List<XpTransaction> todayTxs = xpRepo.findByUserIdAndCreatedAtAfter(userId, startOfDay);
        return todayTxs.stream().mapToInt(XpTransaction::getAmount).sum();
    }
}
