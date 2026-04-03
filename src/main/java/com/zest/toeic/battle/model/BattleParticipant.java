package com.zest.toeic.battle.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "battle_participants")
public class BattleParticipant {

    @Id
    private String id;
    @Indexed
    private String battleId;
    @Indexed
    private String userId;
    private String displayName;

    @Builder.Default
    private int score = 0;
    @Builder.Default
    private int correctCount = 0;
    @Builder.Default
    private double avgResponseTime = 0;
    @Builder.Default
    private int rank = 0;

    @Builder.Default
    private Instant joinedAt = Instant.now();
}
