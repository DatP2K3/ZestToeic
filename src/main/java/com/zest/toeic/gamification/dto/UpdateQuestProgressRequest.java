package com.zest.toeic.gamification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateQuestProgressRequest(
        @NotBlank(message = "Quest type is required") String questType,
        @Min(value = 1, message = "Amount must be at least 1") int amount
) {}
