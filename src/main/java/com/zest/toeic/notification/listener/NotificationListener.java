package com.zest.toeic.notification.listener;
import com.zest.toeic.notification.repository.NotificationRepository;
import com.zest.toeic.notification.model.Notification;
import com.zest.toeic.notification.service.NotificationService;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationListener(NotificationRepository notificationRepository,
                                NotificationService notificationService,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processNotification(String notificationId) {
        log.info("Worker received notification ID: {} from RabbitMQ", notificationId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("Notification {} not found in database. Skipping.", notificationId);
            return;
        }

        Notification notification = notificationOpt.get();
        
        // Skip if user preference is DND (Do Not Disturb) disabled
        if (!notificationService.shouldSendNow(notification.getUserId())) {
            log.info("User {} has notifications disabled currently. Skipping email push.", notification.getUserId());
            return;
        }

        // Fetch user email
        Optional<User> userOpt = userRepository.findById(notification.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String email = user.getEmail();
            
            if (email != null && !email.isEmpty()) {
                // Determine whether it's an email type or generic alert
                log.info("Sending Email notification to {} ({})", user.getId(), email);
                notificationService.sendEmail(email, notification.getTitle(), notification.getMessage());
            } else {
                log.warn("User {} has no valid email address.", user.getId());
            }
        } else {
            log.warn("User {} associated with Notification {} not found.", notification.getUserId(), notificationId);
        }
    }
}
