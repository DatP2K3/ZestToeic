package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.service.AnalyticsService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Admin - Analytics", description = "Analytics Dashboard")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Tổng quan hệ thống (users, questions, sessions)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getOverview()));
    }

    @GetMapping("/learning")
    @Operation(summary = "Chỉ số học tập (avg score, questions per part)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLearningMetrics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getLearningMetrics()));
    }
}
