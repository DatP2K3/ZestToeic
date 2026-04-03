package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.WeeklyGoal;
import com.zest.toeic.gamification.repository.WeeklyGoalRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.model.enums.GoalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class WeeklyGoalService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyGoalService.class);
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final WeeklyGoalRepository weeklyGoalRepository;

    public WeeklyGoalService(WeeklyGoalRepository weeklyGoalRepository) {
        this.weeklyGoalRepository = weeklyGoalRepository;
    }

    public WeeklyGoal setGoal(String userId, int targetQuestions, int targetMinutes) {
        if (targetQuestions <= 0 && targetMinutes <= 0) {
            throw new BadRequestException("Mục tiêu phải lớn hơn 0");
        }

        LocalDate weekStart = getWeekStart();
        return weeklyGoalRepository.findByUserIdAndWeekStart(userId, weekStart)
                .map(existing -> {
                    existing.setTargetQuestions(targetQuestions);
                    existing.setTargetMinutes(targetMinutes);
                    return weeklyGoalRepository.save(existing);
                })
                .orElseGet(() -> weeklyGoalRepository.save(WeeklyGoal.builder()
                        .userId(userId)
                        .targetQuestions(targetQuestions)
                        .targetMinutes(targetMinutes)
                        .weekStart(weekStart)
                        .build()));
    }

    public WeeklyGoal getCurrentGoal(String userId) {
        LocalDate weekStart = getWeekStart();
        return weeklyGoalRepository.findByUserIdAndWeekStart(userId, weekStart).orElse(null);
    }

    public WeeklyGoal updateProgress(String userId, int questionsCompleted, int minutesStudied) {
        LocalDate weekStart = getWeekStart();
        return weeklyGoalRepository.findByUserIdAndWeekStart(userId, weekStart)
                .map(goal -> {
                    goal.setCurrentQuestions(goal.getCurrentQuestions() + questionsCompleted);
                    goal.setCurrentMinutes(goal.getCurrentMinutes() + minutesStudied);
                    if (goal.isCompleted() && GoalStatus.COMPLETED != goal.getStatus()) {
                        goal.setStatus(GoalStatus.COMPLETED);
                        log.info("🎯 Weekly goal completed for user {}", userId);
                    }
                    return weeklyGoalRepository.save(goal);
                })
                .orElse(null);
    }

    public List<WeeklyGoal> getHistory(String userId) {
        return weeklyGoalRepository.findByUserIdOrderByWeekStartDesc(userId);
    }

    public int expireOldGoals() {
        LocalDate weekStart = getWeekStart();
        List<WeeklyGoal> activeGoals = weeklyGoalRepository.findByStatus(GoalStatus.ACTIVE);
        int count = 0;
        for (WeeklyGoal goal : activeGoals) {
            if (goal.getWeekStart().isBefore(weekStart)) {
                goal.setStatus(GoalStatus.EXPIRED);
                weeklyGoalRepository.save(goal);
                count++;
            }
        }
        log.info("📅 Expired {} old weekly goals", count);
        return count;
    }

    private LocalDate getWeekStart() {
        LocalDate today = LocalDate.now(VN_ZONE);
        return today.with(DayOfWeek.MONDAY);
    }
}
