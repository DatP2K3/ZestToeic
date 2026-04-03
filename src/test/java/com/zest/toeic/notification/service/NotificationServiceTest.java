package com.zest.toeic.notification.service;
import com.zest.toeic.notification.repository.NotificationRepository;
import com.zest.toeic.notification.model.Notification;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private NotificationPreferenceService preferenceService;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private JavaMailSender mailSender;
    @InjectMocks private NotificationService service;

    private Notification buildNotification(String id, String userId) {
        Notification n = Notification.builder().userId(userId).build();
        n.setId(id);
        return n;
    }

    @Test
    void send_success() {
        Notification n = buildNotification("n1", "u1");
        when(repository.save(any())).thenReturn(n);
        Notification result = service.send("u1", NotificationType.SYSTEM, "Title", "Msg", Map.of());
        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq("n1"));
    }

    @Test
    void send_rabbitFails_stillSaves() {
        Notification n = buildNotification("n1", "u1");
        when(repository.save(any())).thenReturn(n);
        doThrow(new RuntimeException("RabbitMQ down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
        Notification result = service.send("u1", NotificationType.SYSTEM, "T", "M", Map.of());
        assertNotNull(result);
    }

    @Test
    void sendEmail_success() {
        service.sendEmail("test@test.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_fails_noException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> service.sendEmail("test@test.com", "S", "B"));
    }

    @Test
    void getNotifications() {
        Notification n = buildNotification("1", "u1");
        Page<Notification> page = new PageImpl<>(List.of(n));
        when(repository.findByUserIdOrderByCreatedAtDesc(eq("u1"), any())).thenReturn(page);
        assertEquals(1, service.getNotifications("u1", 0, 20).getContent().size());
    }

    @Test
    void getUnreadCount() {
        when(repository.countByUserIdAndRead("u1", false)).thenReturn(5L);
        assertEquals(5, service.getUnreadCount("u1"));
    }

    @Test
    void markAsRead_success() {
        Notification n = buildNotification("n1", "u1");
        n.setRead(false);
        when(repository.findById("n1")).thenReturn(Optional.of(n));
        when(repository.save(any())).thenReturn(n);
        service.markAsRead("n1");
        assertTrue(n.isRead());
    }

    @Test
    void markAsRead_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.markAsRead("x"));
    }

    @Test
    void markAllAsRead() {
        Notification n1 = buildNotification("n1", "u1");
        n1.setRead(false);
        Notification n2 = buildNotification("n2", "u1");
        n2.setRead(true);
        Page<Notification> page = new PageImpl<>(List.of(n1, n2));
        when(repository.findByUserIdOrderByCreatedAtDesc(eq("u1"), any())).thenReturn(page);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.markAllAsRead("u1");
        assertTrue(n1.isRead());
    }

    @Test
    void shouldSendNow() {
        when(preferenceService.shouldSendNow("u1")).thenReturn(true);
        assertTrue(service.shouldSendNow("u1"));
    }
}
