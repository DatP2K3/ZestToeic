package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.WeeklyGoal;
import com.zest.toeic.shared.model.enums.GoalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyGoalRepository extends MongoRepository<WeeklyGoal, String> {
    Optional<WeeklyGoal> findByUserIdAndWeekStart(String userId, LocalDate weekStart);
    Optional<WeeklyGoal> findByUserIdAndStatus(String userId, GoalStatus status);
    List<WeeklyGoal> findByStatus(GoalStatus status);
    List<WeeklyGoal> findByUserIdOrderByWeekStartDesc(String userId);
}
