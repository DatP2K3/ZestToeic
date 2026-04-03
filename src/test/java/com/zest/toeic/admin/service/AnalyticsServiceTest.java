package com.zest.toeic.admin.service;

import com.zest.toeic.admin.dto.AnalyticsOverviewResponse;
import com.zest.toeic.admin.dto.LearningMetricsResponse;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import com.mongodb.client.MongoCollection;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private MongoCollection<org.bson.Document> mockCollection;

    @InjectMocks private AnalyticsService analyticsService;

    @Test
    void getOverview_ReturnsCombinedStats() {
        when(userRepository.count()).thenReturn(100L);
        when(questionRepository.count()).thenReturn(500L);
        
        when(mongoTemplate.getCollection(anyString())).thenReturn(mockCollection);
        when(mockCollection.countDocuments(any(org.bson.conversions.Bson.class))).thenReturn(50L);
        when(mockCollection.countDocuments()).thenReturn(200L);

        AnalyticsOverviewResponse result = analyticsService.getOverview();

        assertEquals(100L, result.totalUsers());
        assertEquals(500L, result.totalQuestions());
        assertEquals(200L, result.totalTestSessions());
    }

    @Test
    void getLearningMetrics_ReturnsAggregatedData() {
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(Map.class)))
                .thenReturn(new AggregationResults<>(List.of(Map.of("avgScore", 850.5)), new org.bson.Document()));

        LearningMetricsResponse result = analyticsService.getLearningMetrics();

        assertEquals(850.5, result.averageScore());
    }
}
