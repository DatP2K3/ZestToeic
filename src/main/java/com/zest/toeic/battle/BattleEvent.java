package com.zest.toeic.battle;

import lombok.*;

import java.util.List;

/**
 * DTO broadcast từ server → tất cả client trong battle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleEvent {

    private String type; // QUESTION, SCORE_UPDATE, BATTLE_STARTED, BATTLE_ENDED, PLAYER_JOINED
    private String battleId;
    private Object payload;

    public static BattleEvent question(String battleId, Object questionData) {
        return BattleEvent.builder().type("QUESTION").battleId(battleId).payload(questionData).build();
    }

    public static BattleEvent scoreUpdate(String battleId, List<BattleParticipant> leaderboard) {
        return BattleEvent.builder().type("SCORE_UPDATE").battleId(battleId).payload(leaderboard).build();
    }

    public static BattleEvent started(String battleId) {
        return BattleEvent.builder().type("BATTLE_STARTED").battleId(battleId).build();
    }

    public static BattleEvent ended(String battleId, List<BattleParticipant> finalResults) {
        return BattleEvent.builder().type("BATTLE_ENDED").battleId(battleId).payload(finalResults).build();
    }

    public static BattleEvent playerJoined(String battleId, BattleParticipant participant) {
        return BattleEvent.builder().type("PLAYER_JOINED").battleId(battleId).payload(participant).build();
    }
}
