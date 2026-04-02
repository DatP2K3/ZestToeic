package com.zest.toeic.practice.controller;

import com.zest.toeic.shared.ai.AIRouter;
import com.zest.toeic.shared.ai.dto.AIExplanationResponse;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "AI Explanation", description = "AI-powered question explanations")
public class ExplanationController {

    private final AIRouter aiRouter;

    public ExplanationController(AIRouter aiRouter) {
        this.aiRouter = aiRouter;
    }

    @GetMapping("/{questionId}/explanation")
    @Operation(summary = "Lấy giải thích AI cho câu hỏi")
    public ResponseEntity<ApiResponse<AIExplanationResponse>> getExplanation(
            @PathVariable String questionId,
            @RequestParam(defaultValue = "A") String answer) {
        AIExplanationResponse response = aiRouter.explain(questionId, answer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
