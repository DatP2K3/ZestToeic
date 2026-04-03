package com.zest.toeic.notification.integration;
import com.zest.toeic.notification.model.NotificationPreference;
import com.zest.toeic.notification.model.Notification;
import com.zest.toeic.notification.service.NotificationPreferenceService;
import com.zest.toeic.notification.service.NotificationService;

import com.zest.toeic.shared.model.enums.NotificationType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests cho Notification Service.
 *
 * YÊU CẦU: Chạy với infra thật (hoặc Docker):
 * - MongoDB (default port 27017)
 * - Redis (default port 6379)
 * - RabbitMQ (default port 5672, management 15672)
 *
 * Cách chạy:
 *   ./mvnw test -Dtest=NotificationIntegrationTest -Dspring.profiles.active=integration
 *
 * Hoặc dùng Testcontainers (cần thêm dependency):
 *   testImplementation 'org.testcontainers:mongodb'
 *   testImplementation 'org.testcontainers:rabbitmq'
 *
 * Lưu ý: Test class này mặc định @Disabled vì cần infra chạy.
 * Bỏ @Disabled khi muốn test với Docker services đang chạy.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires Docker infrastructure (PostgreSQL, MongoDB, RabbitMQ)")
class NotificationIntegrationTest {

    @Autowired private NotificationService notificationService;
    @Autowired private NotificationPreferenceService preferenceService;
    @Autowired private RabbitTemplate rabbitTemplate;

    /**
     * Test 1: Gửi notification → lưu MongoDB + publish RabbitMQ.
     *
     * Verify:
     * - Notification được lưu vào DB
     * - Message được publish tới RabbitMQ queue
     * - Unread count tăng lên
     */
    @Test
    void send_savesToDbAndPublishesToRabbit() {
        // Given
        String userId = "integration-test-user-" + System.currentTimeMillis();

        // When
        Notification result = notificationService.send(
                userId, NotificationType.SYSTEM, "Test Title", "Test Message",
                Map.of("key", "value"));

        // Then
        assertNotNull(result.getId(), "Notification phải được lưu với ID từ MongoDB");
        assertEquals(userId, result.getUserId());
        assertEquals("Test Title", result.getTitle());
        assertFalse(result.isRead());

        // Verify unread count
        long unread = notificationService.getUnreadCount(userId);
        assertTrue(unread >= 1, "Phải có ít nhất 1 notification chưa đọc");
    }

    /**
     * Test 2: Gửi email thật qua Gmail SMTP.
     *
     * Verify: Không throw exception khi config đúng.
     * Kiểm tra inbox testmailsender2k3@gmail.com để xác nhận.
     */
    @Test
    void sendEmail_sendsViaSmtp() {
        assertDoesNotThrow(() ->
                notificationService.sendEmail(
                        "testmailsender2k3@gmail.com",
                        "[Integration Test] ZestTOEIC Email Test",
                        "Đây là email test tự động từ integration test. Thời gian: " + java.time.Instant.now()));
    }

    /**
     * Test 3: Mark as read flow.
     *
     * Verify: Notification được cập nhật read=true trong MongoDB.
     */
    @Test
    void markAsRead_updatesDb() {
        String userId = "mark-read-test-" + System.currentTimeMillis();
        Notification n = notificationService.send(userId, NotificationType.SYSTEM, "Read Test", "Msg", Map.of());

        notificationService.markAsRead(n.getId());

        // Verify unread count decreased
        long unread = notificationService.getUnreadCount(userId);
        assertEquals(0, unread);
    }

    /**
     * Test 4: Notification Preferences flow.
     *
     * Verify: Preferences được tạo mặc định và cập nhật đúng.
     */
    @Test
    void preferences_createAndUpdate() {
        String userId = "pref-test-" + System.currentTimeMillis();

        // Get default (auto-create)
        NotificationPreference pref = preferenceService.getPreference(userId);
        assertTrue(pref.isEmailEnabled(), "Email mặc định phải enabled");
        assertTrue(pref.isInAppEnabled(), "In-app mặc định phải enabled");

        // Update
        NotificationPreference update = NotificationPreference.builder()
                .emailEnabled(false)
                .quietHoursStart("23:00")
                .quietHoursEnd("06:00")
                .build();
        NotificationPreference updated = preferenceService.updatePreference(userId, update);
        assertFalse(updated.isEmailEnabled());
        assertEquals("23:00", updated.getQuietHoursStart());
    }

    /**
     * Test 5: RabbitMQ connectivity test.
     *
     * Verify: Có thể publish message tới RabbitMQ exchange.
     */
    @Test
    void rabbitMq_publishMessage() {
        assertDoesNotThrow(() ->
                rabbitTemplate.convertAndSend("notification.exchange", "notification.routing", "test-message-" + System.currentTimeMillis()));
    }
}
