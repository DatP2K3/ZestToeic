package com.zest.toeic.admin.dto;

import java.util.List;
import java.util.Map;

public record LearningMetricsResponse(
        double averageScore,
        List<Map<String, Object>> questionsPerPart
) {}
