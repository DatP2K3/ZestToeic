package com.zest.toeic.gamification.scheduler;

import com.zest.toeic.gamification.service.WeeklyGoalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyGoalResetSchedulerTest {

    @Mock private WeeklyGoalService weeklyGoalService;
    @InjectMocks private WeeklyGoalResetScheduler scheduler;

    @Test
    void expireOldWeeklyGoals_CallsService() {
        when(weeklyGoalService.expireOldGoals()).thenReturn(5);

        scheduler.expireOldWeeklyGoals();

        verify(weeklyGoalService, times(1)).expireOldGoals();
    }
}
