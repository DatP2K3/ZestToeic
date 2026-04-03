package com.zest.toeic.practice.service;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class QuestionSelectorService {

    // TOEIC structure: Part → question count for Mock Test
    private static final Map<Integer, Integer> TOEIC_STRUCTURE = Map.of(
            1, 6, 2, 25, 3, 39, 4, 30, 5, 30, 6, 16, 7, 54
    );

    // Part distribution for Placement Test (25 questions)
    private static final Map<Integer, Integer> PLACEMENT_DISTRIBUTION = Map.of(
            1, 2, 2, 2, 3, 3, 4, 3, 5, 5, 6, 3, 7, 7
    );

    private final QuestionRepository questionRepository;

    public QuestionSelectorService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<String> selectAdaptiveQuestions(QuestionDifficulty initialDifficulty) {
        List<String> selectedIds = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : PLACEMENT_DISTRIBUTION.entrySet()) {
            int part = entry.getKey();
            int count = entry.getValue();

            // Try requested difficulty first, fallback to any difficulty
            List<Question> questions = questionRepository
                    .findByPartAndDifficultyAndStatus(part, initialDifficulty, QuestionStatus.PUBLISHED);

            if (questions.size() < count) {
                questions = questionRepository.findByPartAndStatus(part, QuestionStatus.PUBLISHED);
            }

            var shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);
            shuffled.stream().limit(count).map(Question::getId).forEach(selectedIds::add);
        }

        Collections.shuffle(selectedIds);
        return selectedIds;
    }

    public List<String> selectMockQuestions() {
        List<String> selectedIds = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : TOEIC_STRUCTURE.entrySet()) {
            int part = entry.getKey();
            int count = entry.getValue();

            List<Question> questions = questionRepository.findByPartAndStatus(part, QuestionStatus.PUBLISHED);
            var shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);
            shuffled.stream().limit(count).map(Question::getId).forEach(selectedIds::add);
        }

        return selectedIds;
    }
}
