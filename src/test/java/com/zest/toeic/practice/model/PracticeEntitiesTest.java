package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PracticeEntitiesTest {

    @Test
    void testQuestion() {
        Question q = new Question();
        q.setId("q1");
        q.setPart(1);
        q.setCategory("GRAMMAR");
        q.setDifficulty(QuestionDifficulty.EASY);
        q.setContent("C");
        q.setImageUrl("U");
        q.setAudioUrl("A");
        q.setStatus(QuestionStatus.PUBLISHED);
        q.setSource("manual");
        q.setAiConfidence(0.95);

        Question.QuestionOption o = new Question.QuestionOption();
        o.setLabel("A");
        o.setText("Tt");
        q.setOptions(List.of(o));
        q.setCorrectAnswer("A");
        q.setExplanation("E");

        assertEquals("q1", q.getId());
        assertEquals("A", q.getOptions().get(0).getLabel());
        assertNotNull(q.hashCode());
        assertNotNull(q.toString());
        assertNotNull(o.hashCode());
        assertNotNull(o.toString());

        Question.QuestionOption o2 = Question.QuestionOption.builder().label("B").text("T2").build();
        assertEquals("B", o2.getLabel());
    }
}
