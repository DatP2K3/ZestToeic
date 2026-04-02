package com.zest.toeic.battle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleWebSocketControllerTest {

    @Mock private BattleService battleService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private BattleWebSocketController controller;

    @Test
    void submitAnswer_broadcastsScoreUpdate() {
        BattleAnswerMessage msg = BattleAnswerMessage.builder()
                .battleId("b1").userId("u1").questionId("q1")
                .answer("A").correct(true).responseTimeMs(5000).build();
        when(battleService.submitAnswer("b1", "u1", "q1", "A", true, 5000)).thenReturn(15);
        BattleParticipant p = BattleParticipant.builder().userId("u1").score(15).build();
        when(battleService.getResults("b1")).thenReturn(List.of(p));

        controller.submitAnswer(msg);

        ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/battle/b1"), captor.capture());
        assertEquals("SCORE_UPDATE", captor.getValue().getType());
    }

    @Test
    void broadcastQuestion() {
        Map<String, Object> question = Map.of("id", "q1", "content", "What?");
        controller.broadcastQuestion("b1", question);

        ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/battle/b1"), captor.capture());
        assertEquals("QUESTION", captor.getValue().getType());
    }

    @Test
    void broadcastStart() {
        controller.broadcastStart("b1");

        ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/battle/b1"), captor.capture());
        assertEquals("BATTLE_STARTED", captor.getValue().getType());
    }

    @Test
    void broadcastEnd() {
        when(battleService.getResults("b1")).thenReturn(List.of());
        controller.broadcastEnd("b1");

        ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/battle/b1"), captor.capture());
        assertEquals("BATTLE_ENDED", captor.getValue().getType());
    }

    @Test
    void broadcastPlayerJoined() {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("u2").displayName("Player 2").build();
        controller.broadcastPlayerJoined("b1", p);

        ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/battle/b1"), captor.capture());
        assertEquals("PLAYER_JOINED", captor.getValue().getType());
    }
}
