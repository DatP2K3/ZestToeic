package com.zest.toeic.productivity;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
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
class StudyPlanServiceTest {

    @Mock private StudyPlanRepository repository;
    @InjectMocks private StudyPlanService service;

    @Test
    void generatePlan_noExisting() {
        when(repository.findByUserIdAndStatus("u1", "ACTIVE")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudyPlan result = service.generatePlan("u1", 8.0, List.of("Part 5", "Part 3"));
        assertNotNull(result);
        assertTrue(result.isAiGenerated());
        assertEquals(7, result.getDailyPlans().size());
        assertEquals("u1", result.getUserId());
    }

    @Test
    void generatePlan_expiresOldPlan() {
        StudyPlan old = StudyPlan.builder().userId("u1").status("ACTIVE").build();
        when(repository.findByUserIdAndStatus("u1", "ACTIVE")).thenReturn(Optional.of(old));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.generatePlan("u1", 6.0, List.of("Vocabulary"));
        assertEquals("EXPIRED", old.getStatus());
    }

    @Test
    void generatePlan_emptyFocusAreas() {
        when(repository.findByUserIdAndStatus("u1", "ACTIVE")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudyPlan result = service.generatePlan("u1", 10.0, null);
        assertNotNull(result);
        assertFalse(result.getDailyPlans().isEmpty());
    }

    @Test
    void getCurrentPlan() {
        StudyPlan plan = StudyPlan.builder().userId("u1").status("ACTIVE").build();
        when(repository.findByUserIdAndStatus("u1", "ACTIVE")).thenReturn(Optional.of(plan));
        assertTrue(service.getCurrentPlan("u1").isPresent());
    }

    @Test
    void completeTask_success() {
        List<StudyPlan.PlanTask> tasks = new ArrayList<>(List.of(
                StudyPlan.PlanTask.builder().type("PRACTICE").durationMinutes(30).build()));
        List<StudyPlan.DailyPlan> dailyPlans = new ArrayList<>(List.of(
                StudyPlan.DailyPlan.builder().dayOfWeek(1).tasks(tasks).build()));
        StudyPlan plan = StudyPlan.builder().dailyPlans(dailyPlans).build();
        plan.setId("p1");
        when(repository.findById("p1")).thenReturn(Optional.of(plan));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudyPlan result = service.completeTask("p1", 1, 0);
        assertTrue(result.getDailyPlans().get(0).getTasks().get(0).isCompleted());
    }

    @Test
    void completeTask_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.completeTask("x", 1, 0));
    }

    @Test
    void adjustPlan_redistributes() {
        // Create plan with missed tasks on day 1 (Monday)
        List<StudyPlan.PlanTask> day1Tasks = new ArrayList<>(List.of(
                StudyPlan.PlanTask.builder().type("PRACTICE").durationMinutes(30).completed(false).build()));
        List<StudyPlan.PlanTask> day7Tasks = new ArrayList<>(List.of(
                StudyPlan.PlanTask.builder().type("REVIEW").durationMinutes(20).build()));
        List<StudyPlan.DailyPlan> dailyPlans = new ArrayList<>(List.of(
                StudyPlan.DailyPlan.builder().dayOfWeek(1).tasks(day1Tasks).build(),
                StudyPlan.DailyPlan.builder().dayOfWeek(7).tasks(day7Tasks).build()));
        StudyPlan plan = StudyPlan.builder().dailyPlans(dailyPlans).build();
        plan.setId("p1");
        when(repository.findById("p1")).thenReturn(Optional.of(plan));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.adjustPlan("p1");
        verify(repository).save(any());
    }

    @Test
    void adjustPlan_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.adjustPlan("x"));
    }
}
