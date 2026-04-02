package com.zest.toeic.gamification.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeeklyGoalTest {

    @Test
    void getProgressPercent_calculatesCorrectly() {
        WeeklyGoal goal = WeeklyGoal.builder()
                .targetQuestions(100)
                .currentQuestions(50)
                .targetMinutes(60)
                .currentMinutes(15)
                .build();

        double percent = goal.getProgressPercent();
        assertEquals(37.5, percent, 0.1); // (50% + 25%) / 2 = 37.5

        assertTrue(goal.toString().contains("WeeklyGoal"));
        assertNotNull(goal.hashCode());
        WeeklyGoal goal2 = WeeklyGoal.builder()
                .targetQuestions(100)
                .currentQuestions(50)
                .targetMinutes(60)
                .currentMinutes(15)
                .build();
        assertTrue(goal.equals(goal2));
    }

    @Test
    void isCompleted_returnsTrueWhenMet() {
        WeeklyGoal goal = WeeklyGoal.builder()
                .targetQuestions(100)
                .currentQuestions(100)
                .targetMinutes(60)
                .currentMinutes(65)
                .build();
        assertTrue(goal.isCompleted());
    }
}
