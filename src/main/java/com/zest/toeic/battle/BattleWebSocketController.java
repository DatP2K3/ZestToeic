package com.zest.toeic.battle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * WebSocket STOMP Controller cho Battle Royale real-time.
 *
 * Flow:
 * 1. Client connect:  ws://host/ws (SockJS) → STOMP CONNECT
 * 2. Client subscribe: /topic/battle/{battleId}
 * 3. Admin start:      POST /api/v1/admin/battles/{id}/start → broadcastStart()
 * 4. Server broadcast:  QUESTION event → /topic/battle/{battleId}
 * 5. Client answer:    /app/battle.answer → submitAnswer()
 * 6. Server broadcast:  SCORE_UPDATE → /topic/battle/{battleId}
 * 7. Admin end:        POST /api/v1/admin/battles/{id}/end → broadcastEnd()
 */
@Controller
public class BattleWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(BattleWebSocketController.class);

    private final BattleService battleService;
    private final SimpMessagingTemplate messagingTemplate;

    public BattleWebSocketController(BattleService battleService, SimpMessagingTemplate messagingTemplate) {
        this.battleService = battleService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Client gửi đáp án qua STOMP.
     * Destination: /app/battle.answer
     * Broadcast kết quả → /topic/battle/{battleId}
     */
    @MessageMapping("/battle.answer")
    public void submitAnswer(BattleAnswerMessage msg) {
        log.info("Battle {} — user {} answered question {}: {}", msg.getBattleId(), msg.getUserId(), msg.getQuestionId(), msg.getAnswer());

        int scoreEarned = battleService.submitAnswer(
                msg.getBattleId(), msg.getUserId(), msg.getQuestionId(),
                msg.getAnswer(), msg.isCorrect(), msg.getResponseTimeMs());

        // Broadcast updated leaderboard
        List<BattleParticipant> leaderboard = battleService.getResults(msg.getBattleId());
        messagingTemplate.convertAndSend(
                "/topic/battle/" + msg.getBattleId(),
                BattleEvent.scoreUpdate(msg.getBattleId(), leaderboard));
    }

    /**
     * Broadcast câu hỏi mới tới tất cả player.
     * Gọi từ admin flow (REST API hoặc scheduler).
     */
    public void broadcastQuestion(String battleId, Map<String, Object> questionData) {
        log.info("Broadcasting question to battle {}", battleId);
        messagingTemplate.convertAndSend(
                "/topic/battle/" + battleId,
                BattleEvent.question(battleId, questionData));
    }

    /**
     * Broadcast battle started event.
     */
    public void broadcastStart(String battleId) {
        log.info("Broadcasting battle started: {}", battleId);
        messagingTemplate.convertAndSend(
                "/topic/battle/" + battleId,
                BattleEvent.started(battleId));
    }

    /**
     * Broadcast battle ended + final results.
     */
    public void broadcastEnd(String battleId) {
        List<BattleParticipant> results = battleService.getResults(battleId);
        log.info("Broadcasting battle ended: {} — {} participants", battleId, results.size());
        messagingTemplate.convertAndSend(
                "/topic/battle/" + battleId,
                BattleEvent.ended(battleId, results));
    }

    /**
     * Broadcast khi player mới join battle.
     */
    public void broadcastPlayerJoined(String battleId, BattleParticipant participant) {
        messagingTemplate.convertAndSend(
                "/topic/battle/" + battleId,
                BattleEvent.playerJoined(battleId, participant));
    }
}
