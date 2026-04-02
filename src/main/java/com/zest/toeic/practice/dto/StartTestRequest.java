package com.zest.toeic.practice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StartTestRequest {
    private Integer part;        // null = all parts
    private String difficulty;   // null = mixed

    @Min(value = 5, message = "Minimum 5 questions")
    @Max(value = 200, message = "Maximum 200 questions")
    private int questionCount;

    @Min(value = 0, message = "Time limit cannot be negative")
    private int timeLimitMinutes; // 0 = no limit
}
