package com.zest.toeic.practice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitAnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Answer is required")
    private String selectedOption; // A, B, C, D

    private int timeTaken; // seconds
}
