package com.zest.toeic.productivity;

import com.zest.toeic.notification.NotificationService;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock private ReminderRepository repository;
    @Mock private NotificationService notificationService;
    @InjectMocks private ReminderService service;

    @Test
    void create() {
        Reminder r = Reminder.builder().type("DAILY_STUDY").schedule("07:30").message("Time to learn!").build();
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Reminder result = service.create("u1", r);
        assertEquals("u1", result.getUserId());
        assertNotNull(result.getNextSendAt());
    }

    @Test
    void getUserReminders() {
        when(repository.findByUserId("u1")).thenReturn(List.of(Reminder.builder().id("r1").build()));
        assertEquals(1, service.getUserReminders("u1").size());
    }

    @Test
    void update_success() {
        Reminder existing = Reminder.builder().type("DAILY_STUDY").schedule("07:30").message("old").build();
        existing.setId("r1");
        when(repository.findById("r1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reminder update = Reminder.builder().type("CUSTOM").schedule("08:00").message("new").enabled(true).build();
        Reminder result = service.update("r1", update);
        assertEquals("new", result.getMessage());
        assertEquals("08:00", result.getSchedule());
    }

    @Test
    void update_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update("x", new Reminder()));
    }

    @Test
    void delete_success() {
        when(repository.existsById("r1")).thenReturn(true);
        service.delete("r1");
        verify(repository).deleteById("r1");
    }

    @Test
    void delete_notFound() {
        when(repository.existsById("x")).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete("x"));
    }

    @Test
    void processReminders_sendsNotifications() {
        Reminder r = Reminder.builder().id("r1").userId("u1").schedule("07:30").message("Time!").enabled(true)
                .nextSendAt(Instant.now().minusSeconds(60)).build();
        when(repository.findByEnabledTrueAndNextSendAtBefore(any())).thenReturn(List.of(r));
        when(notificationService.shouldSendNow("u1")).thenReturn(true);
        when(notificationService.send(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processReminders();
        verify(notificationService).send(eq("u1"), eq("REMINDER"), anyString(), eq("Time!"), any());
    }

    @Test
    void processReminders_respectsQuietHours() {
        Reminder r = Reminder.builder().id("r1").userId("u1").schedule("07:30").message("Time!").enabled(true)
                .nextSendAt(Instant.now().minusSeconds(60)).build();
        when(repository.findByEnabledTrueAndNextSendAtBefore(any())).thenReturn(List.of(r));
        when(notificationService.shouldSendNow("u1")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processReminders();
        verify(notificationService, never()).send(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void calculateNextSendAt_validTime() {
        Instant next = service.calculateNextSendAt("08:00");
        assertNotNull(next);
        assertTrue(next.isAfter(Instant.now().minusSeconds(1)));
    }

    @Test
    void calculateNextSendAt_invalidFormat() {
        Instant next = service.calculateNextSendAt("invalid");
        assertNotNull(next); // default: tomorrow
    }
}
