package com.zest.toeic.intelligence.controller;

import com.zest.toeic.intelligence.dto.*;
import com.zest.toeic.intelligence.service.IntelligenceService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/intelligence")
@Tag(name = "Intelligence Hub", description = "AI-powered learning insights")
public class IntelligenceController {

    private final IntelligenceService intelligenceService;

    public IntelligenceController(IntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping("/weaknesses")
    @Operation(summary = "Phát hiện điểm yếu — phân tích theo Part và Category")
    public ResponseEntity<ApiResponse<WeaknessResponse>> getWeaknesses(Authentication auth) {
        WeaknessResponse response = intelligenceService.getWeaknesses(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/progress")
    @Operation(summary = "Xu hướng tiến bộ theo tuần")
    public ResponseEntity<ApiResponse<ProgressTrendResponse>> getProgressTrend(
            @RequestParam(defaultValue = "8") int weeks,
            Authentication auth) {
        ProgressTrendResponse response = intelligenceService.getProgressTrend(auth.getName(), weeks);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mistakes")
    @Operation(summary = "Phân tích pattern sai lầm thường gặp")
    public ResponseEntity<ApiResponse<MistakePatternResponse>> getMistakePatterns(Authentication auth) {
        MistakePatternResponse response = intelligenceService.getMistakePatterns(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Đề xuất luyện tập AI dựa trên điểm yếu")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(Authentication auth) {
        RecommendationResponse response = intelligenceService.getRecommendations(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/score-prediction")
    @Operation(summary = "Dự đoán điểm TOEIC dựa trên dữ liệu luyện tập")
    public ResponseEntity<ApiResponse<ScorePredictionResponse>> predictScore(Authentication auth) {
        ScorePredictionResponse response = intelligenceService.predictScore(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
