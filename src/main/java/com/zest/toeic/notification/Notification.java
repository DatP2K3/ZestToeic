package com.zest.toeic.notification;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "notifications")
public class Notification extends BaseDocument {

    @Indexed
    private String userId;
    private String type;       // SYSTEM, SQUAD, BATTLE, REMINDER, ACHIEVEMENT
    private String title;
    private String message;
    private Map<String, Object> data;

    @Builder.Default
    private boolean read = false;

}
