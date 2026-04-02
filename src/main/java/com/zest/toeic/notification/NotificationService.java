package com.zest.toeic.notification;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String EXCHANGE = "notification.exchange";
    private static final String ROUTING_KEY = "notification.send";

    private final NotificationRepository repository;
    private final NotificationPreferenceService preferenceService;
    private final RabbitTemplate rabbitTemplate;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository repository,
                               NotificationPreferenceService preferenceService,
                               RabbitTemplate rabbitTemplate,
                               JavaMailSender mailSender) {
        this.repository = repository;
        this.preferenceService = preferenceService;
        this.rabbitTemplate = rabbitTemplate;
        this.mailSender = mailSender;
    }

    public Notification send(String userId, String type, String title, String message, Map<String, Object> data) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .data(data)
                .build();
        notification = repository.save(notification);

        // Publish to RabbitMQ for async processing (email, push, etc.)
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, notification.getId());
        } catch (Exception e) {
            log.warn("Failed to publish notification to RabbitMQ: {}", e.getMessage());
        }

        log.info("Notification sent to user {} — type: {}, title: {}", userId, type, title);
        return notification;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            msg.setFrom("testmailsender2k3@gmail.com");
            mailSender.send(msg);
            log.info("Email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    public Page<Notification> getNotifications(String userId, int page, int size) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public long getUnreadCount(String userId) {
        return repository.countByUserIdAndRead(userId, false);
    }

    public void markAsRead(String notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        n.setRead(true);
        repository.save(n);
    }

    public void markAllAsRead(String userId) {
        Page<Notification> unread = repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000));
        unread.getContent().stream()
                .filter(n -> !n.isRead())
                .forEach(n -> {
                    n.setRead(true);
                    repository.save(n);
                });
    }

    public boolean shouldSendNow(String userId) {
        return preferenceService.shouldSendNow(userId);
    }
}
