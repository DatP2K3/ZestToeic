package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.dto.AnalyticsOverviewResponse;
import com.zest.toeic.admin.dto.LearningMetricsResponse;
import com.zest.toeic.admin.service.AnalyticsService;
import com.zest.toeic.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock private AnalyticsService analyticsService;
    @InjectMocks private AdminAnalyticsController adminAnalyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminAnalyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getOverview_ReturnsMap() throws Exception {
        AnalyticsOverviewResponse mockResult = new AnalyticsOverviewResponse(100, 50, "2024-01-01", 10, 20, 5);
        when(analyticsService.getOverview()).thenReturn(mockResult);

        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(100));
    }

    @Test
    void getLearningMetrics_ReturnsMap() throws Exception {
        LearningMetricsResponse mockResult = new LearningMetricsResponse(80, java.util.List.of());
        when(analyticsService.getLearningMetrics()).thenReturn(mockResult);

        mockMvc.perform(get("/api/v1/admin/analytics/learning-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageScore").value(80));
    }
}
