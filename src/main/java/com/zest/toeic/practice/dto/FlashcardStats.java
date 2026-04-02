package com.zest.toeic.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FlashcardStats {
    private long total;
    private long learning;
    private long review;
    private long mastered;
    private long dueNow;
}
