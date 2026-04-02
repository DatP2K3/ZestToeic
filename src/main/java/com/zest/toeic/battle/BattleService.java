package com.zest.toeic.battle;

import com.zest.toeic.notification.NotificationService;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BattleService {

    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private final BattleRepository battleRepository;
    private final BattleParticipantRepository participantRepository;
    private final NotificationService notificationService;

    public BattleService(BattleRepository battleRepository,
                         BattleParticipantRepository participantRepository,
                         NotificationService notificationService) {
        this.battleRepository = battleRepository;
        this.participantRepository = participantRepository;
        this.notificationService = notificationService;
    }

    public Battle scheduleBattle(Battle battle) {
        battle.setId(null);
        battle.setStatus("SCHEDULED");
        return battleRepository.save(battle);
    }

    public BattleParticipant register(String battleId, String userId, String displayName) {
        Battle battle = getBattle(battleId);
        if (!"SCHEDULED".equals(battle.getStatus()) && !"REGISTRATION".equals(battle.getStatus())) {
            throw new IllegalStateException("Battle is not open for registration");
        }
        if (participantRepository.countByBattleId(battleId) >= battle.getMaxPlayers()) {
            throw new IllegalStateException("Battle is full");
        }
        if (participantRepository.findByBattleIdAndUserId(battleId, userId).isPresent()) {
            throw new IllegalStateException("Already registered");
        }

        BattleParticipant participant = BattleParticipant.builder()
                .battleId(battleId)
                .userId(userId)
                .displayName(displayName)
                .build();
        return participantRepository.save(participant);
    }

    public Battle startBattle(String battleId) {
        Battle battle = getBattle(battleId);
        battle.setStatus("IN_PROGRESS");
        battle.setStartedAt(Instant.now());
        battle.setCurrentQuestionIndex(0);
        log.info("Battle {} started", battleId);
        return battleRepository.save(battle);
    }

    public int submitAnswer(String battleId, String userId, String questionId, String answer,
                            boolean correct, long responseTimeMs) {
        BattleParticipant participant = participantRepository.findByBattleIdAndUserId(battleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        int scoreEarned = 0;
        if (correct) {
            scoreEarned = 10;
            // Speed bonus
            int seconds = (int) (responseTimeMs / 1000);
            if (seconds < 10) scoreEarned += 5;
            else if (seconds < 20) scoreEarned += 3;
            else if (seconds < 30) scoreEarned += 1;
            participant.setCorrectCount(participant.getCorrectCount() + 1);
        } else {
            scoreEarned = -1;
        }

        participant.setScore(participant.getScore() + scoreEarned);
        // Update avg response time
        double totalResponses = participant.getCorrectCount() + (participant.getScore() < 0 ? 1 : 0);
        if (totalResponses > 0) {
            participant.setAvgResponseTime(
                    (participant.getAvgResponseTime() * (totalResponses - 1) + responseTimeMs) / totalResponses);
        }
        participantRepository.save(participant);
        return scoreEarned;
    }

    public Battle nextQuestion(String battleId) {
        Battle battle = getBattle(battleId);
        battle.setCurrentQuestionIndex(battle.getCurrentQuestionIndex() + 1);
        return battleRepository.save(battle);
    }

    public Battle endBattle(String battleId) {
        Battle battle = getBattle(battleId);
        battle.setStatus("COMPLETED");
        battle.setEndedAt(Instant.now());
        battle = battleRepository.save(battle);

        // Rank participants
        List<BattleParticipant> participants = participantRepository.findByBattleIdOrderByScoreDesc(battleId);
        AtomicInteger rank = new AtomicInteger(1);
        participants.forEach(p -> {
            p.setRank(rank.getAndIncrement());
            participantRepository.save(p);
            // Notify participants
            notificationService.send(p.getUserId(), "BATTLE",
                    "Battle kết thúc!", "Bạn đạt hạng #" + p.getRank(),
                    Map.of("battleId", battleId, "rank", p.getRank()));
        });

        log.info("Battle {} ended with {} participants", battleId, participants.size());
        return battle;
    }

    public List<BattleParticipant> getResults(String battleId) {
        return participantRepository.findByBattleIdOrderByScoreDesc(battleId);
    }

    public List<Battle> getActiveBattles() {
        return battleRepository.findByStatusInOrderByScheduledAtDesc(
                List.of("SCHEDULED", "REGISTRATION", "IN_PROGRESS"));
    }

    public Battle getBattle(String battleId) {
        return battleRepository.findById(battleId)
                .orElseThrow(() -> new ResourceNotFoundException("Battle not found: " + battleId));
    }
}
