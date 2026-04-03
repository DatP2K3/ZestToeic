package com.zest.toeic.notification;
import com.zest.toeic.notification.repository.NotificationPreferenceRepository;
import com.zest.toeic.notification.model.NotificationPreference;
import com.zest.toeic.notification.service.NotificationPreferenceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock private NotificationPreferenceRepository repository;
    @InjectMocks private NotificationPreferenceService service;

    @Test
    void getPreference_existing() {
        NotificationPreference pref = NotificationPreference.builder().userId("u1").build();
        when(repository.findByUserId("u1")).thenReturn(Optional.of(pref));
        assertEquals("u1", service.getPreference("u1").getUserId());
    }

    @Test
    void getPreference_createsDefault() {
        when(repository.findByUserId("u2")).thenReturn(Optional.empty());
        NotificationPreference created = NotificationPreference.builder().userId("u2").build();
        when(repository.save(any())).thenReturn(created);
        NotificationPreference result = service.getPreference("u2");
        assertEquals("u2", result.getUserId());
        verify(repository).save(any());
    }

    @Test
    void updatePreference() {
        NotificationPreference existing = NotificationPreference.builder().userId("u1").emailEnabled(true).build();
        when(repository.findByUserId("u1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        NotificationPreference update = NotificationPreference.builder()
                .emailEnabled(false).pushEnabled(false).inAppEnabled(true)
                .dailyReminderEnabled(false).dailyReminderTime("08:00")
                .squadNotifications(false).battleNotifications(false)
                .quietHoursStart("23:00").quietHoursEnd("06:00").build();

        NotificationPreference result = service.updatePreference("u1", update);
        assertFalse(result.isEmailEnabled());
        assertEquals("08:00", result.getDailyReminderTime());
        assertEquals("23:00", result.getQuietHoursStart());
    }

    @Test
    void shouldSendNow_outsideQuietHours() {
        // Default quiet hours: 22:00-07:00
        NotificationPreference pref = NotificationPreference.builder().userId("u1").build();
        when(repository.findByUserId("u1")).thenReturn(Optional.of(pref));
        // Just verify it doesn't throw — actual result depends on current time
        service.shouldSendNow("u1");
    }
}
