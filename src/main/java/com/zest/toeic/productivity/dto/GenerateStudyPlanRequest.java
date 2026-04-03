package com.zest.toeic.productivity.dto;

import java.util.List;

public record GenerateStudyPlanRequest(
        Double weeklyHours,
        List<String> focusAreas
) {
    public GenerateStudyPlanRequest {
        if (weeklyHours == null) weeklyHours = 8.0;
        if (focusAreas == null) focusAreas = List.of("Part 5", "Part 3");
    }
}
