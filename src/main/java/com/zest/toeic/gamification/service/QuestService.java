package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.DailyQuest;
import com.zest.toeic.gamification.repository.DailyQuestRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class QuestService {

    private static final Logger log = LoggerFactory.getLogger(QuestService.class);
    private static final Random random = new Random();

    private final DailyQuestRepository dailyQuestRepository;
    private final GamificationService gamificationService;

    public QuestService(DailyQuestRepository dailyQuestRepository,
                        GamificationService gamificationService) {
        this.dailyQuestRepository = dailyQuestRepository;
        this.gamificationService = gamificationService;
    }

    public DailyQuest getOrGenerateQuests(String userId) {
        LocalDate today = LocalDate.now();
        return dailyQuestRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> generateQuests(userId, today));
    }

    public DailyQuest updateProgress(String userId, String questType, int amount) {
        DailyQuest dq = getOrGenerateQuests(userId);

        for (DailyQuest.Quest quest : dq.getQuests()) {
            if (quest.getType().equals(questType) && !quest.isCompleted()) {
                quest.setProgress(Math.min(quest.getProgress() + amount, quest.getTarget()));
                if (quest.getProgress() >= quest.getTarget()) {
                    quest.setCompleted(true);
                    log.info("Quest completed: {} for user {}", questType, userId);
                }
                break;
            }
        }

        boolean allDone = dq.getQuests().stream().allMatch(DailyQuest.Quest::isCompleted);
        dq.setAllCompleted(allDone);

        return dailyQuestRepository.save(dq);
    }

    public DailyQuest claimReward(String userId, int questIndex) {
        DailyQuest dq = getOrGenerateQuests(userId);

        if (questIndex < 0 || questIndex >= dq.getQuests().size()) {
            throw new BadRequestException("Quest index không hợp lệ");
        }

        DailyQuest.Quest quest = dq.getQuests().get(questIndex);

        if (!quest.isCompleted()) {
            throw new BadRequestException("Quest chưa hoàn thành");
        }
        if (quest.isClaimed()) {
            throw new BadRequestException("Phần thưởng đã được nhận");
        }

        quest.setClaimed(true);
        gamificationService.awardXp(userId, quest.getXpReward(),
                "QUEST_COMPLETE", null, "Quest: " + quest.getDescription());

        // Bonus when all 3 quests are claimed
        boolean allClaimed = dq.getQuests().stream().allMatch(DailyQuest.Quest::isClaimed);
        if (allClaimed && !dq.isBonusClaimed()) {
            dq.setBonusClaimed(true);
            gamificationService.awardXp(userId, 25,
                    "QUEST_BONUS", null, "All daily quests completed!");
            log.info("Daily quest bonus awarded to user {}", userId);
        }

        return dailyQuestRepository.save(dq);
    }

    private DailyQuest generateQuests(String userId, LocalDate date) {
        int practiceTarget = randomTarget(5, 15);
        int flashcardTarget = randomTarget(3, 8);

        List<DailyQuest.Quest> quests = List.of(
                DailyQuest.Quest.builder()
                        .type("PRACTICE_QUESTIONS")
                        .description("Trả lời " + practiceTarget + " câu hỏi")
                        .target(practiceTarget)
                        .progress(0).completed(false).claimed(false)
                        .xpReward(50)
                        .build(),
                DailyQuest.Quest.builder()
                        .type("REVIEW_FLASHCARDS")
                        .description("Ôn " + flashcardTarget + " flashcards")
                        .target(flashcardTarget)
                        .progress(0).completed(false).claimed(false)
                        .xpReward(30)
                        .build(),
                DailyQuest.Quest.builder()
                        .type("COMPLETE_TEST")
                        .description("Hoàn thành 1 bài test")
                        .target(1)
                        .progress(0).completed(false).claimed(false)
                        .xpReward(50)
                        .build()
        );

        DailyQuest dq = DailyQuest.builder()
                .userId(userId)
                .date(date)
                .quests(new java.util.ArrayList<>(quests))
                .build();

        DailyQuest saved = dailyQuestRepository.save(dq);
        log.info("Generated 3 daily quests for user {} on {}", userId, date);
        return saved;
    }

    private int randomTarget(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
