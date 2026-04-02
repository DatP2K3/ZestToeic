package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.UserStreak;
import com.zest.toeic.gamification.repository.UserStreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private UserStreakRepository userStreakRepository;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private StreakService streakService;

    private UserStreak existingStreak;

    @BeforeEach
    void setUp() {
        existingStreak = UserStreak.builder()
                .userId("user1")
                .currentStreak(5)
                .longestStreak(10)
                .lastActiveDate(LocalDate.now(ZONE).minusDays(1))
                .milestones(new ArrayList<>())
                .build();
    }

    @Test
    void recordActivity_shouldContinueStreak_whenYesterdayActive() {
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(6, result.getCurrentStreak());
        assertEquals(LocalDate.now(ZONE), result.getLastActiveDate());
        verify(userStreakRepository).save(any());
    }

    @Test
    void recordActivity_shouldResetStreak_whenGapMoreThanOneDay() {
        existingStreak.setLastActiveDate(LocalDate.now(ZONE).minusDays(3));
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(1, result.getCurrentStreak());
    }

    @Test
    void recordActivity_shouldIgnoreIfAlreadyRecordedToday() {
        existingStreak.setLastActiveDate(LocalDate.now(ZONE));
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(5, result.getCurrentStreak()); // unchanged
        verify(userStreakRepository, never()).save(any());
    }

    @Test
    void recordActivity_shouldCreateNewStreak_whenNoExisting() {
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.empty());
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(1, result.getCurrentStreak());
        assertEquals(1, result.getLongestStreak());
    }

    @Test
    void recordActivity_shouldUpdateLongestStreak_whenExceedsPrevious() {
        existingStreak.setCurrentStreak(10);
        existingStreak.setLongestStreak(10);
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(11, result.getCurrentStreak());
        assertEquals(11, result.getLongestStreak());
    }

    @Test
    void recordActivity_shouldAwardMilestone_when7DayStreak() {
        existingStreak.setCurrentStreak(6);
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(7, result.getCurrentStreak());
        assertTrue(result.getMilestones().contains(7));
        verify(gamificationService).awardXp(eq("user1"), eq(50), eq("STREAK_MILESTONE"), any(), any());
    }

    @Test
    void recordActivity_shouldNotDuplicateMilestone() {
        existingStreak.setCurrentStreak(6);
        existingStreak.getMilestones().add(7);
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        streakService.recordActivity("user1");

        verify(gamificationService, never()).awardXp(any(), anyInt(), any(), any(), any());
    }

    @Test
    void getStreakInfo_shouldReturnExisting() {
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));

        UserStreak result = streakService.getStreakInfo("user1");

        assertEquals(5, result.getCurrentStreak());
    }

    @Test
    void getStreakInfo_shouldReturnDefault_whenNotFound() {
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.empty());

        UserStreak result = streakService.getStreakInfo("user1");

        assertEquals(0, result.getCurrentStreak());
        assertEquals(0, result.getLongestStreak());
    }

    @Test
    void resetInactiveStreaks_shouldResetActiveStreaks() {
        UserStreak s1 = UserStreak.builder().userId("u1").currentStreak(5).build();
        UserStreak s2 = UserStreak.builder().userId("u2").currentStreak(0).build();
        when(userStreakRepository.findByLastActiveDateBefore(any())).thenReturn(List.of(s1, s2));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        int count = streakService.resetInactiveStreaks();

        assertEquals(1, count);
        assertEquals(0, s1.getCurrentStreak());
        verify(userStreakRepository, times(1)).save(s1);
    }

    @Test
    void recordActivity_shouldAwardMilestone30() {
        existingStreak.setCurrentStreak(29);
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(30, result.getCurrentStreak());
        assertTrue(result.getMilestones().contains(30));
    }

    @Test
    void recordActivity_shouldAwardMilestone100() {
        existingStreak.setCurrentStreak(99);
        existingStreak.setLongestStreak(99);
        when(userStreakRepository.findByUserId("user1")).thenReturn(Optional.of(existingStreak));
        when(userStreakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStreak result = streakService.recordActivity("user1");

        assertEquals(100, result.getCurrentStreak());
        assertTrue(result.getMilestones().contains(100));
    }
}
