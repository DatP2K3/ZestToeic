package com.zest.toeic.battle.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.BattleStatus;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "battles")
public class Battle extends BaseDocument {

    private String title;

    @Indexed
    @Builder.Default
    private BattleStatus status = BattleStatus.SCHEDULED;

    private Instant scheduledAt;
    private Instant startedAt;
    private Instant endedAt;
    private List<String> questionIds;

    @Builder.Default
    private int maxPlayers = 30;

    @Builder.Default
    private int currentQuestionIndex = 0;

    @Builder.Default
    private int timePerQuestion = 30; // seconds
}
