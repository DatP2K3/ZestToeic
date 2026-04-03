package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.dto.AnalyticsOverviewResponse;
import com.zest.toeic.admin.dto.LearningMetricsResponse;
import com.zest.toeic.admin.service.AnalyticsService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Admin - Analytics", description = "Analytics Dashboard")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tổng quan hệ thống")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getOverview()));
    }

    @GetMapping("/learning-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy thống kê học tập")
    public ResponseEntity<ApiResponse<LearningMetricsResponse>> getLearningMetrics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getLearningMetrics()));
    }
}
