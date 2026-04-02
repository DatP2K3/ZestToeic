package com.zest.toeic.admin.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import com.mongodb.client.MongoCollection;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private AnalyticsService analyticsService;

    @Test
    void getOverview_ReturnsStats() {
        when(userRepository.count()).thenReturn(100L);
        when(questionRepository.count()).thenReturn(50L);

        MongoCollection mockCollection = mock(MongoCollection.class);
        when(mongoTemplate.getCollection(any())).thenReturn(mockCollection);
        when(mockCollection.countDocuments()).thenReturn(200L);
        when(mockCollection.countDocuments(any(org.bson.Document.class))).thenReturn(10L);

        Map<String, Object> result = analyticsService.getOverview();
        assertNotNull(result);
    }

    @Test
    void getLearningMetrics_ReturnsMetrics() {
        AggregationResults avgScoreResults = mock(AggregationResults.class);
        when(avgScoreResults.getMappedResults()).thenReturn(List.of(Map.of("averageScore", 650.0)));
        when(mongoTemplate.aggregate(any(org.springframework.data.mongodb.core.aggregation.Aggregation.class), eq("test_sessions"), eq(Map.class)))
                .thenReturn(avgScoreResults);

        AggregationResults partResults = mock(AggregationResults.class);
        when(partResults.getMappedResults()).thenReturn(List.of());
        when(mongoTemplate.aggregate(any(org.springframework.data.mongodb.core.aggregation.Aggregation.class), eq("questions"), eq(Map.class)))
                .thenReturn(partResults);

        Map<String, Object> result = analyticsService.getLearningMetrics();
        assertNotNull(result);
    }
}
