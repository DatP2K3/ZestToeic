package com.zest.toeic.practice.dto;

import java.util.List;

public record GenerateQuestionsResponse(
        int generated,
        int saved,
        int duplicates,
        List<String> errors
) {}
