package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "weekly_goals")
@CompoundIndex(name = "idx_user_week", def = "{'userId': 1, 'weekStart': 1}", unique = true)
public class WeeklyGoal extends BaseDocument {

    private String userId;
    private int targetQuestions;
    private int targetMinutes;
    private int currentQuestions;
    private int currentMinutes;
    private LocalDate weekStart;

    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    public boolean isCompleted() {
        return currentQuestions >= targetQuestions && currentMinutes >= targetMinutes;
    }

    public double getProgressPercent() {
        if (targetQuestions == 0 && targetMinutes == 0) return 100.0;
        double qProgress = targetQuestions > 0 ? (double) currentQuestions / targetQuestions : 1.0;
        double mProgress = targetMinutes > 0 ? (double) currentMinutes / targetMinutes : 1.0;
        return Math.min(100.0, (qProgress + mProgress) / 2.0 * 100.0);
    }
}
