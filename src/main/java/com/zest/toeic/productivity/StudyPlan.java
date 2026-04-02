package com.zest.toeic.productivity;

import com.zest.toeic.shared.model.BaseDocument;
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
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, EXPIRED

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
        private String type; // PRACTICE, REVIEW, TEST, FLASHCARD
        private int part;    // optional
        private int durationMinutes;

        @Builder.Default
        private boolean completed = false;
    }
}
