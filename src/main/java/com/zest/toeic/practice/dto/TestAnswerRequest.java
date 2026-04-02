package com.zest.toeic.practice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TestAnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Answer is required")
    private String selectedOption;

    @Min(value = 0, message = "Time must be non-negative")
    private int timeTaken; // seconds
}
