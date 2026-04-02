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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligenceServiceTest {

    @Mock
    private UserAnswerRepository userAnswerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private IntelligenceService intelligenceService;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        mockQuestion = Question.builder().part(5).category("GRAMMAR").build();
        mockQuestion.setId("q1");
    }

    @Test
    void getWeaknesses_NoAnswers_ReturnsEmpty() {
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of());
        WeaknessResponse res = intelligenceService.getWeaknesses("u1");
        assertEquals(0, res.getTotalWeaknesses());
        assertEquals(0, res.getWeaknesses().size());
    }

    @Test
    void getWeaknesses_UnderSignificance_ReturnsEmpty() {
        // Less than 5 answers for a single part+category
        UserAnswer a = UserAnswer.builder().questionId("q1").correct(true).build();
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(a, a, a, a)); // 4 items
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        WeaknessResponse res = intelligenceService.getWeaknesses("u1");
        assertEquals(0, res.getTotalWeaknesses());
    }

    @Test
    void getWeaknesses_SufficientData_CalculatesCorrectly() {
        List<UserAnswer> answers = new ArrayList<>();
        // 5 answers: 1 correct, 4 wrong => 20% accuracy => CRITICAL
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i == 0).build());
        }

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        WeaknessResponse res = intelligenceService.getWeaknesses("u1");

        assertEquals(1, res.getTotalWeaknesses());
        assertEquals(20.0, res.getWeaknesses().get(0).getAccuracy());
        assertEquals("CRITICAL", res.getWeaknesses().get(0).getSeverity());
        assertEquals("GRAMMAR", res.getWeaknesses().get(0).getCategory());
        assertEquals(5, res.getWeaknesses().get(0).getPart());
    }

    @Test
    void getProgressTrend_EmptyAnswers_ReturnsEmptyData() {
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of());

        ProgressTrendResponse res = intelligenceService.getProgressTrend("u1", 4);

        assertEquals(0, res.getWeeksIncluded());
    }

    @Test
    void getProgressTrend_CalculatesTrendByWeek() {
        Instant now = Instant.now();
        Instant lastWeek = now.minus(7, ChronoUnit.DAYS);

        // One answer this week (correct) -> 100%
        // Two answers last week (both incorrect) -> 0%
        UserAnswer thisWeekA = UserAnswer.builder().correct(true).timeTaken(10).build();
        thisWeekA.setCreatedAt(now);

        UserAnswer lastWeekA1 = UserAnswer.builder().correct(false).timeTaken(10).build();
        lastWeekA1.setCreatedAt(lastWeek);
        UserAnswer lastWeekA2 = UserAnswer.builder().correct(false).timeTaken(20).build();
        lastWeekA2.setCreatedAt(lastWeek);

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(thisWeekA, lastWeekA1, lastWeekA2));

        ProgressTrendResponse res = intelligenceService.getProgressTrend("u1", 4);

        assertEquals(2, res.getWeeksIncluded()); // 2 weeks grouped
        
        // Asserting the sorted order (last week is first chronologically)
        ProgressTrendResponse.WeeklyData firstWk = res.getTrend().get(0);
        assertEquals(2, firstWk.getTotalAnswered());
        assertEquals(0.0, firstWk.getAccuracy());
        assertEquals(15.0, firstWk.getAverageTimeTaken()); // (10+20)/2

        ProgressTrendResponse.WeeklyData secondWk = res.getTrend().get(1);
        assertEquals(1, secondWk.getTotalAnswered());
        assertEquals(100.0, secondWk.getAccuracy());
        assertEquals(10.0, secondWk.getAverageTimeTaken());
        assertEquals(100.0, secondWk.getDeltaAccuracy()); // Went from 0 to 100
    }

    @Test
    void getMistakePatterns_NoIncorrect_ReturnsEmpty() {
        UserAnswer correct = UserAnswer.builder().correct(true).build();
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(correct));

        MistakePatternResponse res = intelligenceService.getMistakePatterns("u1");

        assertEquals(0, res.getTotalMistakes());
    }

    @Test
    void getWeaknesses_SeverityClassification_And_NullCategory() {
        Question qNullCat = Question.builder().part(1).category(null).build();
        qNullCat.setId("q2");

        List<UserAnswer> answers = new ArrayList<>();
        // 5 answers for q2: 4 correct (80% -> LOW)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q2").correct(i < 4).build());
        }
        // 5 answers for q1 (mockQuestion): 2 correct (40% -> HIGH)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i < 2).build());
        }
        // 5 answers for an unknown question (should skip)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("qMissing").correct(true).build());
        }

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        when(questionRepository.findById("q2")).thenReturn(Optional.of(qNullCat));
        when(questionRepository.findById("qMissing")).thenReturn(Optional.empty());

        WeaknessResponse res = intelligenceService.getWeaknesses("u1");

        // Should skip qMissing, so only 2 weakness items.
        // Also it sorts by accuracy ascending, so 40% (q1) should be first, 80% (q2) second.
        assertEquals(2, res.getTotalWeaknesses());
        assertEquals(40.0, res.getWeaknesses().get(0).getAccuracy());
        assertEquals("HIGH", res.getWeaknesses().get(0).getSeverity());
        
        assertEquals(80.0, res.getWeaknesses().get(1).getAccuracy());
        assertEquals("LOW", res.getWeaknesses().get(1).getSeverity());
        assertEquals("GENERAL", res.getWeaknesses().get(1).getCategory()); // fallback default
    }

    @Test
    void getWeaknesses_MediumSeverity() {
        List<UserAnswer> answers = new ArrayList<>();
        // 5 answers: 3 correct (60% -> MEDIUM)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i < 3).build());
        }
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        WeaknessResponse res = intelligenceService.getWeaknesses("u1");
        assertEquals("MEDIUM", res.getWeaknesses().get(0).getSeverity());
    }

    @Test
    void getProgressTrend_WithNullDates_IgnoresThem() {
        UserAnswer noDate = UserAnswer.builder().correct(true).build();
        // createdAt is null implicitly by builder
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(noDate));

        ProgressTrendResponse res = intelligenceService.getProgressTrend("u1", 4);
        assertEquals(0, res.getWeeksIncluded());
    }

    @Test
    void getMistakePatterns_NullQuestionAndNullCategory() {
        Question qNullCat = Question.builder().part(1).category(null).build();
        qNullCat.setId("q2");

        UserAnswer wrong1 = UserAnswer.builder().questionId("qMissing").correct(false).build();
        UserAnswer wrong2 = UserAnswer.builder().questionId("q2").correct(false).build();
        
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(wrong1, wrong2));
        when(questionRepository.findById("qMissing")).thenReturn(Optional.empty());
        when(questionRepository.findById("q2")).thenReturn(Optional.of(qNullCat));

        MistakePatternResponse res = intelligenceService.getMistakePatterns("u1");

        // "qMissing" is skipped. Total mistakes is still 2 but only 1 pattern is formed.
        // Wait, total mistakes is calculated from incorrectAnswers.size(), so it's 2.
        assertEquals(2, res.getTotalMistakes());
        assertEquals(1, res.getPatterns().size());

        MistakePatternResponse.MistakePattern pattern = res.getPatterns().get(0);
        assertEquals("GENERAL", pattern.getCategory()); // fallback default
    }

    @Test
    void getRecommendations_shouldSuggestPracticeForWeaknesses() {
        List<UserAnswer> answers = new ArrayList<>();
        // Part 5: 1 correct, 4 wrong => 20% (CRITICAL)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i == 0).build());
        }
        // Part 1: 3 correct, 2 wrong => 60% (MEDIUM)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q2").correct(i < 3).build());
        }
        
        Question q2 = Question.builder().part(1).category("LISTENING").build();
        q2.setId("q2");

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        when(questionRepository.findById("q2")).thenReturn(Optional.of(q2));

        RecommendationResponse res = intelligenceService.getRecommendations("u1");

        assertEquals(2, res.getRecommendations().size());
        
        // Items sorted by accuracy (Severe to Less Severe implies lower accuracy first)
        RecommendationResponse.Recommendation first = res.getRecommendations().get(0);
        assertEquals("CRITICAL", first.getPriority());
        assertEquals("PRACTICE", first.getType());
        assertEquals("+30-50 points potential", first.getEstimatedImpact());

        RecommendationResponse.Recommendation second = res.getRecommendations().get(1);
        assertEquals("MEDIUM", second.getPriority());
        assertEquals("+5-15 points potential", second.getEstimatedImpact());
    }

    @Test
    void getRecommendations_shouldSuggestTestIfNoSevereWeaknesses() {
        List<UserAnswer> answers = new ArrayList<>();
        // Part 5: 4 correct, 1 wrong => 80% (LOW)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i < 4).build());
        }

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        RecommendationResponse res = intelligenceService.getRecommendations("u1");

        assertEquals(1, res.getRecommendations().size());
        assertEquals("TEST", res.getRecommendations().get(0).getType());
        assertEquals("LOW", res.getRecommendations().get(0).getPriority());
    }

    @Test
    void getRecommendations_EmptyData_SuggestsTest() {
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of());

        RecommendationResponse res = intelligenceService.getRecommendations("u1");
        
        assertEquals(1, res.getRecommendations().size());
        assertEquals("TEST", res.getRecommendations().get(0).getType());
    }

    @Test
    void getRecommendations_HighSeverity() {
        List<UserAnswer> answers = new ArrayList<>();
        // Part 5: 2 correct, 3 wrong => 40% (HIGH)
        for (int i = 0; i < 5; i++) {
            answers.add(UserAnswer.builder().questionId("q1").correct(i < 2).build());
        }

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));

        RecommendationResponse res = intelligenceService.getRecommendations("u1");

        assertEquals(1, res.getRecommendations().size());
        assertEquals("HIGH", res.getRecommendations().get(0).getPriority());
        assertEquals("+15-30 points potential", res.getRecommendations().get(0).getEstimatedImpact());
    }

    @Test
    void predictScore_NoData() {
        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of());
        ScorePredictionResponse res = intelligenceService.predictScore("u1");

        assertEquals(10, res.getPredictedScore());
        assertEquals(5, res.getListeningScore());
        assertEquals(5, res.getReadingScore());
        assertEquals(0.0, res.getConfidence());
    }

    @Test
    void predictScore_WithData() {
        List<UserAnswer> answers = new ArrayList<>();
        // Listening Part 1: 8 correct, 2 wrong -> 80%
        Question qL = Question.builder().part(1).build();
        for (int i = 0; i < 10; i++) {
            answers.add(UserAnswer.builder().questionId("qL").correct(i < 8).build());
        }
        // Reading Part 5: 6 correct, 4 wrong -> 60%
        Question qR = Question.builder().part(5).build();
        for (int i = 0; i < 10; i++) {
            answers.add(UserAnswer.builder().questionId("qR").correct(i < 6).build());
        }
        // Unknown question -> should be skipped
        answers.add(UserAnswer.builder().questionId("unknown").correct(true).build());

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("qL")).thenReturn(Optional.of(qL));
        when(questionRepository.findById("qR")).thenReturn(Optional.of(qR));
        when(questionRepository.findById("unknown")).thenReturn(Optional.empty());

        ScorePredictionResponse res = intelligenceService.predictScore("u1");

        // Total answers: 21, Confidence = Math.min(1.0, 21/200) * 100 = 10.5%
        assertEquals(10.5, res.getConfidence());

        // L_Score = 5 + 80 * 490 / 100 = 397
        // R_Score = 5 + 60 * 490 / 100 = 299
        // Total = 397 + 299 = 696
        assertEquals(397, res.getListeningScore());
        assertEquals(299, res.getReadingScore());
        assertEquals(696, res.getPredictedScore());

        // Tips should include "Practice more questions" and "Focus on Reading practice"
        assertTrue(res.getImprovementTips().stream().anyMatch(t -> t.contains("Reading practice")));
        assertTrue(res.getImprovementTips().stream().anyMatch(t -> t.contains("Practice more")));
    }

    @Test
    void predictScore_ListeningLowerThanReading() {
        List<UserAnswer> answers = new ArrayList<>();
        // Listening Part 1: 40%
        Question qL = Question.builder().part(1).build();
        for (int i = 0; i < 10; i++) {
            answers.add(UserAnswer.builder().questionId("qL").correct(i < 4).build());
        }
        // Reading Part 5: 60%
        Question qR = Question.builder().part(5).build();
        for (int i = 0; i < 10; i++) {
            answers.add(UserAnswer.builder().questionId("qR").correct(i < 6).build());
        }

        when(userAnswerRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(answers);
        when(questionRepository.findById("qL")).thenReturn(Optional.of(qL));
        when(questionRepository.findById("qR")).thenReturn(Optional.of(qR));

        ScorePredictionResponse res = intelligenceService.predictScore("u1");

        assertTrue(res.getImprovementTips().stream().anyMatch(t -> t.contains("Listening practice")));
        assertTrue(res.getImprovementTips().stream().anyMatch(t -> t.contains("Target one part"))); // since score < 600
    }
}
