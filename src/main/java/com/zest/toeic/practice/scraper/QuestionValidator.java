package com.zest.toeic.practice.scraper;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionValidator {

    public boolean isValid(Question question) {
        if (question.getContent() == null || question.getContent().isBlank()) return false;
        if (question.getOptions() == null || question.getOptions().size() != 4) return false;
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) return false;
        if (question.getPart() < 1 || question.getPart() > 7) return false;

        // Verify correct answer is one of A, B, C, D
        String answer = question.getCorrectAnswer().toUpperCase();
        if (!List.of("A", "B", "C", "D").contains(answer)) return false;

        // Check options have text
        return question.getOptions().stream()
                .allMatch(o -> o.getText() != null && !o.getText().isBlank());
    }

    public boolean isDuplicate(Question question, QuestionRepository repo) {
        // Use optimized DB query to prevent OOM
        return repo.existsByContentIgnoreCase(question.getContent());
    }
}
