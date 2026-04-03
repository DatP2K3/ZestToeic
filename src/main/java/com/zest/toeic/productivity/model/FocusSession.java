package com.zest.toeic.productivity.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "focus_sessions")
public class FocusSession extends BaseDocument {

    private String userId;
    private Instant startedAt;
    private Instant endedAt;

    @Builder.Default
    private int durationMinutes = 50;
    @Builder.Default
    private int breakMinutes = 10;

    private double actualMinutes;
    private String taskDescription;

    @Builder.Default
    private boolean completed = false;
}
