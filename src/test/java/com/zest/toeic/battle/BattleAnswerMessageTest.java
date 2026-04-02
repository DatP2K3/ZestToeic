package com.zest.toeic.battle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleAnswerMessageTest {

    @Test
    void builderAndAccessors() {
        BattleAnswerMessage msg = BattleAnswerMessage.builder()
                .battleId("b1").userId("u1").questionId("q1")
                .answer("A").correct(true).responseTimeMs(5000).build();

        assertEquals("b1", msg.getBattleId());
        assertEquals("u1", msg.getUserId());
        assertEquals("q1", msg.getQuestionId());
        assertEquals("A", msg.getAnswer());
        assertTrue(msg.isCorrect());
        assertEquals(5000, msg.getResponseTimeMs());
        assertNotNull(msg.toString());
    }

    @Test
    void noArgsConstructor() {
        BattleAnswerMessage msg = new BattleAnswerMessage();
        msg.setBattleId("b1");
        msg.setUserId("u1");
        msg.setAnswer("B");
        msg.setCorrect(false);
        msg.setResponseTimeMs(10000);
        msg.setQuestionId("q2");

        assertEquals("b1", msg.getBattleId());
        assertFalse(msg.isCorrect());
    }
}
