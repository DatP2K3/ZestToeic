package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.DailyQuest;
import com.zest.toeic.gamification.repository.DailyQuestRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.model.enums.QuestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private DailyQuestRepository dailyQuestRepository;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private QuestService questService;

    private DailyQuest mockDailyQuest;

    @BeforeEach
    void setUp() {
        List<DailyQuest.Quest> quests = new ArrayList<>(List.of(
                DailyQuest.Quest.builder()
                        .type(QuestType.PRACTICE_QUESTIONS).description("Trả lời 10 câu hỏi")
                        .target(10).progress(0).completed(false).claimed(false).xpReward(50)
                        .build(),
                DailyQuest.Quest.builder()
                        .type(QuestType.REVIEW_FLASHCARDS).description("Ôn 5 flashcards")
                        .target(5).progress(0).completed(false).claimed(false).xpReward(30)
                        .build(),
                DailyQuest.Quest.builder()
                        .type(QuestType.COMPLETE_TEST).description("Hoàn thành 1 bài test")
                        .target(1).progress(0).completed(false).claimed(false).xpReward(50)
                        .build()
        ));

        mockDailyQuest = DailyQuest.builder()
                .userId("u1")
                .date(LocalDate.now())
                .quests(quests)
                .build();
        mockDailyQuest.setId("dq1");
    }

    // ═══ getOrGenerateQuests ═══

    @Test
    void getOrGenerateQuests_ExistingQuest_ReturnsExisting() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));

        DailyQuest result = questService.getOrGenerateQuests("u1");

        assertEquals("dq1", result.getId());
        assertEquals(3, result.getQuests().size());
        verify(dailyQuestRepository, never()).save(any());
    }

    @Test
    void getOrGenerateQuests_NoExisting_GeneratesNew() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.getOrGenerateQuests("u1");

        assertNotNull(result);
        assertEquals(3, result.getQuests().size());
        assertEquals(QuestType.PRACTICE_QUESTIONS, result.getQuests().get(0).getType());
        assertEquals(QuestType.REVIEW_FLASHCARDS, result.getQuests().get(1).getType());
        assertEquals(QuestType.COMPLETE_TEST, result.getQuests().get(2).getType());

        // Verify description matches target (bug fix validation)
        DailyQuest.Quest practiceQuest = result.getQuests().get(0);
        assertTrue(practiceQuest.getDescription().contains(String.valueOf(practiceQuest.getTarget())));

        verify(dailyQuestRepository).save(any(DailyQuest.class));
    }

    // ═══ updateProgress ═══

    @Test
    void updateProgress_IncreasesProgress() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.updateProgress("u1", "PRACTICE_QUESTIONS", 3);

        assertEquals(3, result.getQuests().get(0).getProgress());
        assertFalse(result.getQuests().get(0).isCompleted());
    }

    @Test
    void updateProgress_CompletesQuest() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.updateProgress("u1", "PRACTICE_QUESTIONS", 10);

        assertEquals(10, result.getQuests().get(0).getProgress());
        assertTrue(result.getQuests().get(0).isCompleted());
    }

    @Test
    void updateProgress_OverTarget_CappedAtTarget() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.updateProgress("u1", "PRACTICE_QUESTIONS", 20);

        assertEquals(10, result.getQuests().get(0).getProgress()); // capped at target (10)
        assertTrue(result.getQuests().get(0).isCompleted());
    }

    @Test
    void updateProgress_AllCompleted_SetsAllCompleted() {
        // Pre-complete first 2 quests
        mockDailyQuest.getQuests().get(0).setCompleted(true);
        mockDailyQuest.getQuests().get(0).setProgress(10);
        mockDailyQuest.getQuests().get(1).setCompleted(true);
        mockDailyQuest.getQuests().get(1).setProgress(5);

        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.updateProgress("u1", "COMPLETE_TEST", 1);

        assertTrue(result.isAllCompleted());
    }

    // ═══ claimReward ═══

    @Test
    void claimReward_InvalidIndex_ThrowsBadRequest() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));

        assertThrows(BadRequestException.class,
                () -> questService.claimReward("u1", 5));
    }

    @Test
    void claimReward_NegativeIndex_ThrowsBadRequest() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));

        assertThrows(BadRequestException.class,
                () -> questService.claimReward("u1", -1));
    }

    @Test
    void claimReward_NotCompleted_ThrowsBadRequest() {
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));

        assertThrows(BadRequestException.class,
                () -> questService.claimReward("u1", 0));
    }

    @Test
    void claimReward_AlreadyClaimed_ThrowsBadRequest() {
        mockDailyQuest.getQuests().get(0).setCompleted(true);
        mockDailyQuest.getQuests().get(0).setClaimed(true);
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));

        assertThrows(BadRequestException.class,
                () -> questService.claimReward("u1", 0));
    }

    @Test
    void claimReward_Success_AwardsXp() {
        mockDailyQuest.getQuests().get(0).setCompleted(true);
        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.claimReward("u1", 0);

        assertTrue(result.getQuests().get(0).isClaimed());
        verify(gamificationService).awardXp(eq("u1"), eq(50), eq("QUEST_COMPLETE"), isNull(), anyString());
    }

    @Test
    void claimReward_AllClaimed_AwardsBonus() {
        // All quests completed and first 2 claimed
        for (DailyQuest.Quest q : mockDailyQuest.getQuests()) {
            q.setCompleted(true);
        }
        mockDailyQuest.getQuests().get(0).setClaimed(true);
        mockDailyQuest.getQuests().get(1).setClaimed(true);

        when(dailyQuestRepository.findByUserIdAndDate("u1", LocalDate.now()))
                .thenReturn(Optional.of(mockDailyQuest));
        when(dailyQuestRepository.save(any(DailyQuest.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyQuest result = questService.claimReward("u1", 2);

        assertTrue(result.isBonusClaimed());
        // Regular reward + bonus = 2 calls to awardXp
        verify(gamificationService, times(2)).awardXp(eq("u1"), anyInt(), anyString(), any(), anyString());
    }
}
