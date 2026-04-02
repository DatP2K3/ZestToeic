package com.zest.toeic.notification;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "notification_preferences")
public class NotificationPreference extends BaseDocument {

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private boolean emailEnabled = true;
    @Builder.Default
    private boolean pushEnabled = true;
    @Builder.Default
    private boolean inAppEnabled = true;
    @Builder.Default
    private boolean dailyReminderEnabled = true;
    @Builder.Default
    private String dailyReminderTime = "07:30";
    @Builder.Default
    private boolean squadNotifications = true;
    @Builder.Default
    private boolean battleNotifications = true;
    @Builder.Default
    private String quietHoursStart = "22:00";
    @Builder.Default
    private String quietHoursEnd = "07:00";
}
