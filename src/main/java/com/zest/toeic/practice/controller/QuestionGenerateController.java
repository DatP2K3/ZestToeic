package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.ai.AIQuestionGenerator;
import com.zest.toeic.practice.dto.GenerateQuestionsResponse;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Content Pipeline", description = "Scraping & importing questions")
public class QuestionGenerateController {

    private final AIQuestionGenerator aiQuestionGenerator;

    public QuestionGenerateController(AIQuestionGenerator aiQuestionGenerator) {
        this.aiQuestionGenerator = aiQuestionGenerator;
    }

    @GetMapping("/generate")
    @Operation(summary = "Tạo câu hỏi bằng AI")
    public ResponseEntity<ApiResponse<GenerateQuestionsResponse>> generateQuestions(
            @RequestParam int part,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "1") int count) {
        
        GenerateQuestionsResponse result = aiQuestionGenerator.generate(part, difficulty, count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
