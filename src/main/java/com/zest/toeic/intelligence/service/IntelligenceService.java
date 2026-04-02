package com.zest.toeic.intelligence.service;

import com.zest.toeic.intelligence.dto.MistakePatternResponse;
import com.zest.toeic.intelligence.dto.ProgressTrendResponse;
import com.zest.toeic.intelligence.dto.RecommendationResponse;
import com.zest.toeic.intelligence.dto.ScorePredictionResponse;
import com.zest.toeic.intelligence.dto.WeaknessResponse;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.model.UserAnswer;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class IntelligenceService {

    private static final int MIN_ANSWERS_FOR_SIGNIFICANCE = 5;
    private static final int MAX_WEAKNESSES = 10;
    private static final int MAX_PATTERNS = 5;

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;

    public IntelligenceService(UserAnswerRepository userAnswerRepository, QuestionRepository questionRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * S-3-1: Weakness Detection — analyze answers, rank by severity.
     */
    public WeaknessResponse getWeaknesses(String userId) {
        List<UserAnswer> allAnswers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (allAnswers.isEmpty()) {
            return WeaknessResponse.builder().totalWeaknesses(0).weaknesses(List.of()).build();
        }

        // Group by part+category
        Map<String, int[]> groupStats = new LinkedHashMap<>(); // key -> [total, correct]
        Map<String, Integer> groupPart = new HashMap<>();
        Map<String, String> groupCategory = new HashMap<>();

        for (UserAnswer answer : allAnswers) {
            Question q = questionRepository.findById(answer.getQuestionId()).orElse(null);
            if (q == null) continue;

            String key = q.getPart() + "|" + (q.getCategory() != null ? q.getCategory() : "GENERAL");
            groupPart.putIfAbsent(key, q.getPart());
            groupCategory.putIfAbsent(key, q.getCategory() != null ? q.getCategory() : "GENERAL");
            groupStats.computeIfAbsent(key, k -> new int[]{0, 0});
            groupStats.get(key)[0]++;
            if (answer.isCorrect()) groupStats.get(key)[1]++;
        }

        List<WeaknessResponse.Weakness> weaknesses = groupStats.entrySet().stream()
                .filter(e -> e.getValue()[0] >= MIN_ANSWERS_FOR_SIGNIFICANCE)
                .map(e -> {
                    double accuracy = (double) e.getValue()[1] / e.getValue()[0] * 100;
                    String severity = classifySeverity(accuracy);
                    return WeaknessResponse.Weakness.builder()
                            .part(groupPart.get(e.getKey()))
                            .category(groupCategory.get(e.getKey()))
                            .accuracy(Math.round(accuracy * 100.0) / 100.0)
                            .totalAnswered(e.getValue()[0])
                            .severity(severity)
                            .build();
                })
                .sorted(Comparator.comparingDouble(WeaknessResponse.Weakness::getAccuracy))
                .limit(MAX_WEAKNESSES)
                .toList();

        return WeaknessResponse.builder()
                .totalWeaknesses(weaknesses.size())
                .weaknesses(weaknesses)
                .build();
    }

    /**
     * S-3-2: Progress Trend — weekly accuracy chart data.
     */
    public ProgressTrendResponse getProgressTrend(String userId, int weeks) {
        weeks = Math.max(1, Math.min(weeks, 52));
        Instant startDate = Instant.now().minus(weeks * 7L, ChronoUnit.DAYS);

        List<UserAnswer> answers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(startDate))
                .toList();

        // Group by week
        Map<LocalDate, List<UserAnswer>> weeklyGroups = new LinkedHashMap<>();
        for (UserAnswer a : answers) {
            if (a.getCreatedAt() == null) continue;
            LocalDate date = LocalDate.ofInstant(a.getCreatedAt(), ZoneId.systemDefault());
            // Start of week (Monday)
            LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
            weeklyGroups.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(a);
        }

        List<ProgressTrendResponse.WeeklyData> trend = new ArrayList<>();
        Double previousAccuracy = null;

        List<LocalDate> sortedWeeks = new ArrayList<>(weeklyGroups.keySet());
        Collections.sort(sortedWeeks);

        for (LocalDate weekStart : sortedWeeks) {
            List<UserAnswer> weekAnswers = weeklyGroups.get(weekStart);
            long total = weekAnswers.size();
            long correct = weekAnswers.stream().filter(UserAnswer::isCorrect).count();
            double accuracy = total > 0 ? Math.round((double) correct / total * 10000.0) / 100.0 : 0;
            double avgTime = weekAnswers.stream()
                    .mapToInt(UserAnswer::getTimeTaken)
                    .average().orElse(0);

            Double delta = previousAccuracy != null ? Math.round((accuracy - previousAccuracy) * 100.0) / 100.0 : null;
            previousAccuracy = accuracy;

            trend.add(ProgressTrendResponse.WeeklyData.builder()
                    .weekStart(weekStart.toString())
                    .totalAnswered(total)
                    .correctCount(correct)
                    .accuracy(accuracy)
                    .averageTimeTaken(Math.round(avgTime * 100.0) / 100.0)
                    .deltaAccuracy(delta)
                    .build());
        }

        return ProgressTrendResponse.builder()
                .weeksIncluded(trend.size())
                .trend(trend)
                .build();
    }

    /**
     * S-3-3: Mistake Pattern Analysis — cluster errors by category.
     */
    public MistakePatternResponse getMistakePatterns(String userId) {
        List<UserAnswer> incorrectAnswers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(a -> !a.isCorrect())
                .toList();

        if (incorrectAnswers.isEmpty()) {
            return MistakePatternResponse.builder().totalMistakes(0).patterns(List.of()).build();
        }

        // Group by category+part
        Map<String, List<String>> patternQuestionIds = new LinkedHashMap<>();
        Map<String, Integer> patternParts = new HashMap<>();
        Map<String, String> patternCategories = new HashMap<>();

        for (UserAnswer a : incorrectAnswers) {
            Question q = questionRepository.findById(a.getQuestionId()).orElse(null);
            if (q == null) continue;
            String category = q.getCategory() != null ? q.getCategory() : "GENERAL";
            String key = category + "|" + q.getPart();
            patternParts.putIfAbsent(key, q.getPart());
            patternCategories.putIfAbsent(key, category);
            patternQuestionIds.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getQuestionId());
        }

        long totalMistakes = incorrectAnswers.size();

        List<MistakePatternResponse.MistakePattern> patterns = patternQuestionIds.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(MAX_PATTERNS)
                .map(e -> MistakePatternResponse.MistakePattern.builder()
                        .category(patternCategories.get(e.getKey()))
                        .part(patternParts.get(e.getKey()))
                        .mistakeCount(e.getValue().size())
                        .percentage(Math.round((double) e.getValue().size() / totalMistakes * 10000.0) / 100.0)
                        .exampleQuestionIds(e.getValue().stream().distinct().limit(3).toList())
                        .build())
                .toList();

        return MistakePatternResponse.builder()
                .totalMistakes(totalMistakes)
                .patterns(patterns)
                .build();
    }

    /**
     * FR-012: AI Recommendations — suggest practice based on weaknesses.
     */
    public RecommendationResponse getRecommendations(String userId) {
        WeaknessResponse weaknesses = getWeaknesses(userId);

        List<RecommendationResponse.Recommendation> recs = new ArrayList<>();

        for (WeaknessResponse.Weakness w : weaknesses.getWeaknesses()) {
            String priority = w.getSeverity();
            if ("LOW".equals(priority)) continue; // No recommendations for low severity

            int questionsToSuggest = switch (priority) {
                case "CRITICAL" -> 20;
                case "HIGH" -> 15;
                default -> 10; // MEDIUM
            };

            String impact = switch (priority) {
                case "CRITICAL" -> "+30-50 points potential";
                case "HIGH" -> "+15-30 points potential";
                default -> "+5-15 points potential";
            };

            recs.add(RecommendationResponse.Recommendation.builder()
                    .type("PRACTICE")
                    .description("Focus on Part " + w.getPart() + " - " + w.getCategory()
                            + " (accuracy " + w.getAccuracy() + "%). Practice " + questionsToSuggest + " questions.")
                    .targetPart(w.getPart())
                    .priority(priority)
                    .estimatedImpact(impact)
                    .build());
        }

        if (recs.isEmpty()) {
            recs.add(RecommendationResponse.Recommendation.builder()
                    .type("TEST")
                    .description("Great job! Take a full mock test to maintain your level.")
                    .targetPart(0)
                    .priority("LOW")
                    .estimatedImpact("Maintain current score")
                    .build());
        }

        return RecommendationResponse.builder().recommendations(recs).build();
    }

    /**
     * FR-013: Score Prediction — estimate TOEIC score based on part accuracy.
     */
    public ScorePredictionResponse predictScore(String userId) {
        List<UserAnswer> allAnswers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (allAnswers.isEmpty()) {
            return ScorePredictionResponse.builder()
                    .predictedScore(10)
                    .listeningScore(5)
                    .readingScore(5)
                    .confidence(0.0)
                    .improvementTips(List.of("Start practicing to get an accurate prediction."))
                    .build();
        }

        // Group by part
        Map<Integer, int[]> partStats = new HashMap<>(); // part -> [total, correct]
        for (UserAnswer a : allAnswers) {
            Question q = questionRepository.findById(a.getQuestionId()).orElse(null);
            if (q == null) continue;
            partStats.computeIfAbsent(q.getPart(), k -> new int[]{0, 0});
            partStats.get(q.getPart())[0]++;
            if (a.isCorrect()) partStats.get(q.getPart())[1]++;
        }

        // Listening: Parts 1-4, Reading: Parts 5-7
        double listeningAccuracy = calculateSectionAccuracy(partStats, 1, 4);
        double readingAccuracy = calculateSectionAccuracy(partStats, 5, 7);

        // TOEIC scoring: each section 5-495, total 10-990
        int listeningScore = (int) Math.round(5 + listeningAccuracy * 490 / 100);
        int readingScore = (int) Math.round(5 + readingAccuracy * 490 / 100);
        int totalScore = listeningScore + readingScore;

        // Confidence based on sample size
        double confidence = Math.min(1.0, (double) allAnswers.size() / 200) * 100;
        confidence = Math.round(confidence * 10.0) / 10.0;

        List<String> tips = new ArrayList<>();
        if (listeningAccuracy < readingAccuracy) {
            tips.add("Focus on Listening practice (Parts 1-4) to improve your overall score.");
        } else if (readingAccuracy < listeningAccuracy) {
            tips.add("Focus on Reading practice (Parts 5-7) to improve your overall score.");
        }
        if (allAnswers.size() < 100) {
            tips.add("Practice more questions to increase prediction accuracy.");
        }
        if (totalScore < 600) {
            tips.add("Target one part at a time — start with your strongest part to build momentum.");
        }

        return ScorePredictionResponse.builder()
                .predictedScore(totalScore)
                .listeningScore(listeningScore)
                .readingScore(readingScore)
                .confidence(confidence)
                .improvementTips(tips)
                .build();
    }

    private double calculateSectionAccuracy(Map<Integer, int[]> partStats, int fromPart, int toPart) {
        int total = 0, correct = 0;
        for (int p = fromPart; p <= toPart; p++) {
            int[] stats = partStats.get(p);
            if (stats != null) {
                total += stats[0];
                correct += stats[1];
            }
        }
        return total > 0 ? (double) correct / total * 100 : 50.0; // Default 50% if no data
    }

    private String classifySeverity(double accuracy) {
        if (accuracy < 40) return "CRITICAL";
        if (accuracy < 60) return "HIGH";
        if (accuracy < 75) return "MEDIUM";
        return "LOW";
    }
}
