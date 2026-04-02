package com.zest.toeic.community.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.repository.SquadRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SquadStreakScheduler {

    private static final Logger log = LoggerFactory.getLogger(SquadStreakScheduler.class);

    private final SquadRepository squadRepository;
    private final UserAnswerRepository userAnswerRepository;

    public SquadStreakScheduler(SquadRepository squadRepository,
                               UserAnswerRepository userAnswerRepository) {
        this.squadRepository = squadRepository;
        this.userAnswerRepository = userAnswerRepository;
    }

    /**
     * Chạy mỗi đêm 00:05 để kiểm tra squad streak.
     * Nếu TẤT CẢ thành viên đã hoạt động hôm qua → streak +1
     * Nếu BẤT KỲ thành viên nào không hoạt động → streak reset về 0
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void checkSquadStreaks() {
        log.info("🔄 Starting squad streak check...");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        Instant startOfYesterday = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfYesterday = startOfYesterday.plus(1, ChronoUnit.DAYS);

        List<Squad> allSquads = squadRepository.findAll();
        int updated = 0;
        int reset = 0;

        for (Squad squad : allSquads) {
            if (squad.getMembers().isEmpty()) continue;

            boolean allActive = squad.getMembers().stream().allMatch(member -> {
                long answerCount = userAnswerRepository.countByUserIdAndCreatedAtBetween(
                        member.getUserId(), startOfYesterday, endOfYesterday);
                return answerCount > 0;
            });

            if (allActive) {
                squad.setStreak(squad.getStreak() + 1);
                updated++;
            } else {
                if (squad.getStreak() > 0) reset++;
                squad.setStreak(0);
            }

            squad.setLastStreakCheckDate(yesterday);
            squadRepository.save(squad);
        }

        log.info("✅ Squad streak check done: {} squads processed, {} streak++, {} reset",
                allSquads.size(), updated, reset);
    }
}
