package com.zest.toeic.productivity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reminders")
public class Reminder {

    @Id
    private String id;
    private String userId;
    private String type; // DAILY_STUDY, STREAK_WARNING, SQUAD_ALERT, CUSTOM
    private String schedule; // HH:mm format
    private String message;

    @Builder.Default
    private boolean enabled = true;

    private Instant lastSentAt;
    private Instant nextSendAt;
}
