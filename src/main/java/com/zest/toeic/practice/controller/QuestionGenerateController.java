package com.zest.toeic.practice.controller;

import com.zest.toeic.shared.ai.AIQuestionGenerator;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Content Pipeline", description = "Scraping & importing questions")
public class QuestionGenerateController {

    private final AIQuestionGenerator aiQuestionGenerator;

    public QuestionGenerateController(AIQuestionGenerator aiQuestionGenerator) {
        this.aiQuestionGenerator = aiQuestionGenerator;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate câu hỏi bằng AI — Gemini (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateQuestions(
            @RequestParam(defaultValue = "5") int part,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "10") int count) {
        Map<String, Object> result = aiQuestionGenerator.generate(part, difficulty, count);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }
}
