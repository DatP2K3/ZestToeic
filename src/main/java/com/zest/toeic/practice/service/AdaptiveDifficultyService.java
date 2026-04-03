package com.zest.toeic.practice.service;

import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdaptiveDifficultyService {

    public void updateAdaptiveDifficulty(TestSession session) {
        List<TestSession.TestAnswer> answers = session.getAnswers();
        int answered = answers.size();

        if (answered >= 5) {
            long correct = answers.stream().filter(TestSession.TestAnswer::isCorrect).count();
            double rate = (double) correct / answered;

            if (rate >= 0.75) {
                session.setCurrentDifficulty(QuestionDifficulty.HARD);
            } else if (rate <= 0.40) {
                session.setCurrentDifficulty(QuestionDifficulty.EASY);
            } else {
                session.setCurrentDifficulty(QuestionDifficulty.MEDIUM);
            }
        }
    }
}
