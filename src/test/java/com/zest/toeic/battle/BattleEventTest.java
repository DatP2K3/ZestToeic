package com.zest.toeic.battle;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BattleEventTest {

    @Test
    void question() {
        Map<String, Object> data = Map.of("id", "q1", "content", "Test?");
        BattleEvent e = BattleEvent.question("b1", data);
        assertEquals("QUESTION", e.getType());
        assertEquals("b1", e.getBattleId());
        assertNotNull(e.getPayload());
    }

    @Test
    void scoreUpdate() {
        BattleEvent e = BattleEvent.scoreUpdate("b1", List.of());
        assertEquals("SCORE_UPDATE", e.getType());
    }

    @Test
    void started() {
        BattleEvent e = BattleEvent.started("b1");
        assertEquals("BATTLE_STARTED", e.getType());
        assertNull(e.getPayload());
    }

    @Test
    void ended() {
        BattleEvent e = BattleEvent.ended("b1", List.of());
        assertEquals("BATTLE_ENDED", e.getType());
    }

    @Test
    void playerJoined() {
        BattleParticipant p = BattleParticipant.builder().userId("u1").build();
        BattleEvent e = BattleEvent.playerJoined("b1", p);
        assertEquals("PLAYER_JOINED", e.getType());
    }

    @Test
    void builderAndAccessors() {
        BattleEvent e = new BattleEvent();
        e.setType("T");
        e.setBattleId("b");
        e.setPayload("p");
        assertEquals("T", e.getType());
        assertEquals("b", e.getBattleId());
        assertEquals("p", e.getPayload());
        assertNotNull(e.toString());
    }
}
