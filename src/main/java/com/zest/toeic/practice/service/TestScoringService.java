package com.zest.toeic.practice.service;

import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestScoringService {

    private static final Logger log = LoggerFactory.getLogger(TestScoringService.class);

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public TestScoringService(UserRepository userRepository, QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    public int calculateToeicScore(TestSession session) {
        // Weighted scoring: EASY=1, MEDIUM=2, HARD=3
        double totalWeight = 0;
        double earnedWeight = 0;

        for (TestSession.TestAnswer answer : session.getAnswers()) {
            QuestionDifficulty diff = answer.getDifficulty() != null ? answer.getDifficulty() : QuestionDifficulty.MEDIUM;
            double weight = switch (diff) {
                case EASY -> 1.0;
                case HARD -> 3.0;
                default -> 2.0;
            };
            totalWeight += weight;
            if (answer.isCorrect()) {
                earnedWeight += weight;
            }
        }

        if (totalWeight == 0) return 10;

        double ratio = earnedWeight / totalWeight;
        // TOEIC scale: 10-990, map ratio to this range
        int score = (int) Math.round(10 + ratio * 980);
        // Round to nearest 5
        return (score / 5) * 5;
    }

    public String assignLevel(int score) {
        if (score >= 800) return "EXPERT";
        if (score >= 600) return "ADVANCED";
        if (score >= 400) return "INTERMEDIATE";
        return "NOVICE";
    }

    public void updateUserLevel(String userId, int score, String level) {
        userRepository.findById(userId).ifPresent(user -> {
            int numericLevel = switch (level) {
                case "EXPERT" -> 4;
                case "ADVANCED" -> 3;
                case "INTERMEDIATE" -> 2;
                default -> 1;
            };
            user.setLevel(numericLevel);
            userRepository.save(user);
            log.info("User {} placement: score={}, level={} ({})", userId, score, level, numericLevel);
        });
    }

    public Map<Integer, TestResult.PartScore> buildPartScores(TestSession session) {
        Map<Integer, int[]> partData = new LinkedHashMap<>(); // part -> [total, correct]

        List<String> questionIds = session.getAnswers().stream()
                .map(TestSession.TestAnswer::getQuestionId)
                .toList();
                
        Map<String, com.zest.toeic.practice.model.Question> questionMap = new java.util.HashMap<>();
        questionRepository.findAllById(questionIds).forEach(q -> questionMap.put(q.getId(), q));

        for (TestSession.TestAnswer answer : session.getAnswers()) {
            com.zest.toeic.practice.model.Question q = questionMap.get(answer.getQuestionId());
            if (q != null) {
                partData.computeIfAbsent(q.getPart(), k -> new int[]{0, 0});
                int[] data = partData.get(q.getPart());
                data[0]++;
                if (answer.isCorrect()) data[1]++;
            }
        }

        return partData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> TestResult.PartScore.builder()
                                .total(e.getValue()[0])
                                .correct(e.getValue()[1])
                                .accuracy(Math.round((double) e.getValue()[1] / e.getValue()[0] * 10000.0) / 100.0)
                                .build(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
