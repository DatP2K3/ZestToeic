package com.zest.toeic.battle;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
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

    @Builder.Default
    private String status = "SCHEDULED"; // SCHEDULED, REGISTRATION, IN_PROGRESS, COMPLETED

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
