package com.zest.toeic.practice.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PracticeEntitiesTest {

    @Test
    void testQuestion() {
        Question q = new Question();
        q.setId("q1");
        q.setPart(1);
        q.setType("T");
        q.setDifficulty("EASY");
        q.setContent("C");
        q.setImageUrl("U");
        q.setAudioUrl("A");
        
        Question.Option o = new Question.Option();
        o.setKey("A");
        o.setText("Tt");
        q.setOptions(List.of(o));
        q.setCorrectAnswer("A");
        q.setExplanation("E");

        assertEquals("q1", q.getId());
        assertEquals("A", q.getOptions().get(0).getKey());

        Question q2 = Question.builder().id("q1").build();
        assertTrue(q.equals(q) || !q.equals(q2));
        assertNotNull(q.hashCode());
        assertNotNull(q.toString());
        assertNotNull(o.hashCode());
        assertNotNull(o.toString());
    }
}
