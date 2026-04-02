package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.WeeklyGoal;
import com.zest.toeic.gamification.repository.WeeklyGoalRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyGoalServiceTest {

    @Mock private WeeklyGoalRepository weeklyGoalRepository;
    @InjectMocks private WeeklyGoalService weeklyGoalService;

    private LocalDate getWeekStart() {
        return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).with(DayOfWeek.MONDAY);
    }

    @Test
    void setGoal_createsNewGoal() {
        when(weeklyGoalRepository.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.empty());
        when(weeklyGoalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyGoal result = weeklyGoalService.setGoal("u1", 50, 120);
        assertEquals(50, result.getTargetQuestions());
        assertEquals(120, result.getTargetMinutes());
    }

    @Test
    void setGoal_updatesExistingGoal() {
        WeeklyGoal existing = WeeklyGoal.builder().userId("u1").targetQuestions(30).weekStart(getWeekStart()).build();
        when(weeklyGoalRepository.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.of(existing));
        when(weeklyGoalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyGoal result = weeklyGoalService.setGoal("u1", 70, 200);
        assertEquals(70, result.getTargetQuestions());
    }

    @Test
    void setGoal_throwsOnInvalidTarget() {
        assertThrows(BadRequestException.class, () -> weeklyGoalService.setGoal("u1", 0, 0));
    }

    @Test
    void getCurrentGoal_returnsNull_whenNoGoal() {
        when(weeklyGoalRepository.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.empty());
        assertNull(weeklyGoalService.getCurrentGoal("u1"));
    }

    @Test
    void updateProgress_addsAndCompletes() {
        WeeklyGoal goal = WeeklyGoal.builder().userId("u1").targetQuestions(10).targetMinutes(60)
                .currentQuestions(8).currentMinutes(55).weekStart(getWeekStart()).build();
        when(weeklyGoalRepository.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.of(goal));
        when(weeklyGoalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyGoal result = weeklyGoalService.updateProgress("u1", 3, 10);
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void updateProgress_returnsNull_whenNoGoal() {
        when(weeklyGoalRepository.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.empty());
        assertNull(weeklyGoalService.updateProgress("u1", 1, 1));
    }

    @Test
    void expireOldGoals_expiresCorrectly() {
        WeeklyGoal oldGoal = WeeklyGoal.builder().userId("u1").weekStart(getWeekStart().minusWeeks(2)).status("ACTIVE").build();
        when(weeklyGoalRepository.findByStatus("ACTIVE")).thenReturn(List.of(oldGoal));
        when(weeklyGoalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        int expired = weeklyGoalService.expireOldGoals();
        assertEquals(1, expired);
        assertEquals("EXPIRED", oldGoal.getStatus());
    }

    @Test
    void getHistory_returnsList() {
        when(weeklyGoalRepository.findByUserIdOrderByWeekStartDesc("u1")).thenReturn(List.of());
        assertTrue(weeklyGoalService.getHistory("u1").isEmpty());
    }
}
