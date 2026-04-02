package com.zest.toeic.admin.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
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

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalUsers", userRepository.count());
        overview.put("totalQuestions", questionRepository.count());
        overview.put("date", LocalDate.now(VN_ZONE).toString());

        // Total test sessions
        long totalSessions = mongoTemplate.getCollection("test_sessions").countDocuments();
        overview.put("totalTestSessions", totalSessions);

        // Total answers
        long totalAnswers = mongoTemplate.getCollection("user_answers").countDocuments();
        overview.put("totalAnswers", totalAnswers);

        // Active subscriptions
        long premiumUsers = mongoTemplate.getCollection("subscriptions").countDocuments(
                new org.bson.Document("status", "ACTIVE")
        );
        overview.put("premiumUsers", premiumUsers);

        return overview;
    }

    public Map<String, Object> getLearningMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // Average score from test results
        try {
            Aggregation avgScoreAgg = Aggregation.newAggregation(
                    Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("status").is("COMPLETED")),
                    Aggregation.group().avg("score").as("avgScore")
            );
            AggregationResults<Map> avgResult = mongoTemplate.aggregate(avgScoreAgg, "test_sessions", Map.class);
            if (!avgResult.getMappedResults().isEmpty()) {
                metrics.put("averageScore", avgResult.getMappedResults().get(0).getOrDefault("avgScore", 0));
            }
        } catch (Exception e) {
            metrics.put("averageScore", 0);
        }

        // Questions per part distribution
        try {
            Aggregation partAgg = Aggregation.newAggregation(
                    Aggregation.group("part").count().as("count"),
                    Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "_id")
            );
            AggregationResults<Map> partResult = mongoTemplate.aggregate(partAgg, "questions", Map.class);
            metrics.put("questionsPerPart", partResult.getMappedResults());
        } catch (Exception e) {
            metrics.put("questionsPerPart", java.util.List.of());
        }

        return metrics;
    }
}
