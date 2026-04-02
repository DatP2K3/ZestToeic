package com.zest.toeic.notification;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class NotificationPreferenceService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final NotificationPreferenceRepository repository;

    public NotificationPreferenceService(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    public NotificationPreference getPreference(String userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference pref = NotificationPreference.builder().userId(userId).build();
                    return repository.save(pref);
                });
    }

    public NotificationPreference updatePreference(String userId, NotificationPreference update) {
        NotificationPreference existing = getPreference(userId);
        existing.setEmailEnabled(update.isEmailEnabled());
        existing.setPushEnabled(update.isPushEnabled());
        existing.setInAppEnabled(update.isInAppEnabled());
        existing.setDailyReminderEnabled(update.isDailyReminderEnabled());
        existing.setDailyReminderTime(update.getDailyReminderTime());
        existing.setSquadNotifications(update.isSquadNotifications());
        existing.setBattleNotifications(update.isBattleNotifications());
        existing.setQuietHoursStart(update.getQuietHoursStart());
        existing.setQuietHoursEnd(update.getQuietHoursEnd());
        return repository.save(existing);
    }

    public boolean shouldSendNow(String userId) {
        NotificationPreference pref = getPreference(userId);
        ZonedDateTime now = ZonedDateTime.now(VN_ZONE);
        LocalTime currentTime = now.toLocalTime();

        LocalTime quietStart = LocalTime.parse(pref.getQuietHoursStart());
        LocalTime quietEnd = LocalTime.parse(pref.getQuietHoursEnd());

        // Quiet hours span midnight (e.g. 22:00 - 07:00)
        if (quietStart.isAfter(quietEnd)) {
            return currentTime.isAfter(quietEnd) && currentTime.isBefore(quietStart);
        }
        // Quiet hours within same day (e.g. 13:00 - 15:00)
        return currentTime.isBefore(quietStart) || currentTime.isAfter(quietEnd);
    }
}
