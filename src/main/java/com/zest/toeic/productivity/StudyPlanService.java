package com.zest.toeic.productivity;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class StudyPlanService {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanService.class);

    private final StudyPlanRepository repository;

    public StudyPlanService(StudyPlanRepository repository) {
        this.repository = repository;
    }

    public StudyPlan generatePlan(String userId, double weeklyHours, List<String> focusAreas) {
        // Expire existing active plan
        repository.findByUserIdAndStatus(userId, "ACTIVE").ifPresent(existing -> {
            existing.setStatus("EXPIRED");
            repository.save(existing);
        });

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        int totalMinutes = (int) (weeklyHours * 60);
        int minutesPerDay = totalMinutes / 7;

        // AI-assisted: distribute tasks based on focus areas and weakness
        List<StudyPlan.DailyPlan> dailyPlans = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            List<StudyPlan.PlanTask> tasks = generateDailyTasks(minutesPerDay, focusAreas, day);
            dailyPlans.add(StudyPlan.DailyPlan.builder()
                    .dayOfWeek(day)
                    .tasks(tasks)
                    .build());
        }

        StudyPlan plan = StudyPlan.builder()
                .userId(userId)
                .weekStart(weekStart)
                .totalHoursTarget(weeklyHours)
                .focusAreas(focusAreas)
                .dailyPlans(dailyPlans)
                .aiGenerated(true)
                .build();

        log.info("Generated study plan for user {} — {} hours/week, focus: {}", userId, weeklyHours, focusAreas);
        return repository.save(plan);
    }

    public Optional<StudyPlan> getCurrentPlan(String userId) {
        return repository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    public StudyPlan completeTask(String planId, int dayOfWeek, int taskIndex) {
        StudyPlan plan = repository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        plan.getDailyPlans().stream()
                .filter(d -> d.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .ifPresent(d -> {
                    if (taskIndex >= 0 && taskIndex < d.getTasks().size()) {
                        d.getTasks().get(taskIndex).setCompleted(true);
                    }
                });

        return repository.save(plan);
    }

    public StudyPlan adjustPlan(String planId) {
        StudyPlan plan = repository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        int todayDow = LocalDate.now().getDayOfWeek().getValue();

        // Calculate missed minutes from past days
        int missedMinutes = plan.getDailyPlans().stream()
                .filter(d -> d.getDayOfWeek() < todayDow)
                .flatMap(d -> d.getTasks().stream())
                .filter(t -> !t.isCompleted())
                .mapToInt(StudyPlan.PlanTask::getDurationMinutes)
                .sum();

        if (missedMinutes <= 0) return plan;

        // Redistribute to remaining days
        long remainingDays = plan.getDailyPlans().stream()
                .filter(d -> d.getDayOfWeek() >= todayDow)
                .count();

        if (remainingDays == 0) return plan;

        int extraPerDay = missedMinutes / (int) remainingDays;

        plan.getDailyPlans().stream()
                .filter(d -> d.getDayOfWeek() >= todayDow)
                .forEach(d -> {
                    if (!d.getTasks().isEmpty()) {
                        StudyPlan.PlanTask lastTask = d.getTasks().get(d.getTasks().size() - 1);
                        lastTask.setDurationMinutes(lastTask.getDurationMinutes() + extraPerDay);
                    }
                });

        log.info("Adjusted plan {} — redistributed {} missed minutes", planId, missedMinutes);
        return repository.save(plan);
    }

    private List<StudyPlan.PlanTask> generateDailyTasks(int totalMinutes, List<String> focusAreas, int dayOfWeek) {
        List<StudyPlan.PlanTask> tasks = new ArrayList<>();
        if (focusAreas == null || focusAreas.isEmpty()) {
            focusAreas = List.of("Part 5", "Part 3", "Vocabulary");
        }

        int minutesPerTask = totalMinutes / focusAreas.size();

        for (String area : focusAreas) {
            String type = area.toLowerCase().contains("vocab") || area.toLowerCase().contains("flashcard")
                    ? "FLASHCARD" : "PRACTICE";
            int part = extractPart(area);

            tasks.add(StudyPlan.PlanTask.builder()
                    .type(type)
                    .part(part)
                    .durationMinutes(Math.max(minutesPerTask, 10))
                    .build());
        }

        // Add a review session on even days
        if (dayOfWeek % 2 == 0 && totalMinutes > 30) {
            tasks.add(StudyPlan.PlanTask.builder()
                    .type("REVIEW")
                    .durationMinutes(15)
                    .build());
        }

        return tasks;
    }

    private int extractPart(String area) {
        try {
            return Integer.parseInt(area.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
