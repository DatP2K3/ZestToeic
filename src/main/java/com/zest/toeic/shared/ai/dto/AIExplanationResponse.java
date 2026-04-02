package com.zest.toeic.shared.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AIExplanationResponse {
    private String questionId;
    private String userAnswer;
    private String explanation;
    private String provider;  // GEMINI, CACHE, STATIC
    private boolean cached;
}
