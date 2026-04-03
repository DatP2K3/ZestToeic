package com.zest.toeic.admin.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import com.zest.toeic.admin.dto.AnalyticsOverviewResponse;
import com.zest.toeic.admin.dto.LearningMetricsResponse;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;

    public AnalyticsService(UserRepository userRepository,
                            QuestionRepository questionRepository,
                            MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public AnalyticsOverviewResponse getOverview() {
        long totalUsers = userRepository.count();
        long totalQuestions = questionRepository.count();
        String date = LocalDate.now(VN_ZONE).toString();

        long totalSessions = mongoTemplate.getCollection("test_sessions").countDocuments();
        long totalAnswers = mongoTemplate.getCollection("user_answers").countDocuments();
        long premiumUsers = mongoTemplate.getCollection("subscriptions").countDocuments(
                new org.bson.Document("status", "ACTIVE")
        );

        return new AnalyticsOverviewResponse(totalUsers, totalQuestions, date, totalSessions, totalAnswers, premiumUsers);
    }

    public LearningMetricsResponse getLearningMetrics() {
        double averageScore = 0;
        List<Map<String, Object>> questionsPerPart = List.of();

        // Average score from test results
        try {
            Aggregation avgScoreAgg = Aggregation.newAggregation(
                    Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("status").is("COMPLETED")),
                    Aggregation.group().avg("score").as("avgScore")
            );
            @SuppressWarnings("unchecked")
            AggregationResults<Map<String, Object>> avgResult = mongoTemplate.aggregate(avgScoreAgg, "test_sessions", (Class<Map<String, Object>>) (Class<?>) Map.class);
            if (!avgResult.getMappedResults().isEmpty()) {
                averageScore = ((Number) avgResult.getMappedResults().get(0).getOrDefault("avgScore", 0)).doubleValue();
            }
        } catch (Exception e) {
            averageScore = 0;
        }

        // Questions per part distribution
        try {
            Aggregation partAgg = Aggregation.newAggregation(
                    Aggregation.group("part").count().as("count"),
                    Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "_id")
            );
            @SuppressWarnings("unchecked")
            AggregationResults<Map<String, Object>> partResult = mongoTemplate.aggregate(partAgg, "questions", (Class<Map<String, Object>>) (Class<?>) Map.class);
            questionsPerPart = partResult.getMappedResults();
        } catch (Exception e) {
            questionsPerPart = List.of();
        }

        return new LearningMetricsResponse(averageScore, questionsPerPart);
    }
}
