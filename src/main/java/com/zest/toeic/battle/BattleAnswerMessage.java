package com.zest.toeic.battle;

import lombok.*;

/**
 * DTO gửi từ client khi submit đáp án trong Battle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleAnswerMessage {
    private String battleId;
    private String userId;
    private String questionId;
    private String answer;
    private boolean correct;
    private long responseTimeMs;
}
