package com.zest.toeic.productivity.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.GoalStatus;
import com.zest.toeic.shared.model.enums.PlanTaskType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "study_plans")
public class StudyPlan extends BaseDocument {

    private String userId;
    private LocalDate weekStart; // Monday
    private double totalHoursTarget;
    private List<String> focusAreas;
    private List<DailyPlan> dailyPlans;

    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Builder.Default
    private boolean aiGenerated = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPlan {
        private int dayOfWeek; // 1=Mon..7=Sun
        private List<PlanTask> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanTask {
        private PlanTaskType type;
        private int part;    // optional
        private int durationMinutes;

        @Builder.Default
        private boolean completed = false;
    }
}
