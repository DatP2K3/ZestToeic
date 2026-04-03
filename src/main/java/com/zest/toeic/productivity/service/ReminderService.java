package com.zest.toeic.productivity.service;
import com.zest.toeic.productivity.repository.ReminderRepository;
import com.zest.toeic.productivity.model.Reminder;

import com.zest.toeic.notification.service.NotificationService;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ReminderRepository repository;
    private final NotificationService notificationService;

    public ReminderService(ReminderRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public Reminder create(String userId, Reminder reminder) {
        reminder.setId(null);
        reminder.setUserId(userId);
        reminder.setNextSendAt(calculateNextSendAt(reminder.getSchedule()));
        return repository.save(reminder);
    }

    public List<Reminder> getUserReminders(String userId) {
        return repository.findByUserId(userId);
    }

    public Reminder update(String id, Reminder update) {
        Reminder existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        existing.setType(update.getType());
        existing.setSchedule(update.getSchedule());
        existing.setMessage(update.getMessage());
        existing.setEnabled(update.isEnabled());
        existing.setNextSendAt(calculateNextSendAt(update.getSchedule()));
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder not found: " + id);
        }
        repository.deleteById(id);
    }

    @Scheduled(fixedRate = 60000) // every minute
    public void processReminders() {
        List<Reminder> dueReminders = repository.findByEnabledTrueAndNextSendAtBefore(Instant.now());
        for (Reminder reminder : dueReminders) {
            if (notificationService.shouldSendNow(reminder.getUserId())) {
                notificationService.send(reminder.getUserId(), NotificationType.REMINDER,
                        "⏰ Nhắc nhở", reminder.getMessage(), Map.of("reminderId", reminder.getId()));
                log.info("Reminder sent to user {}: {}", reminder.getUserId(), reminder.getMessage());
            }
            reminder.setLastSentAt(Instant.now());
            reminder.setNextSendAt(calculateNextSendAt(reminder.getSchedule()));
            repository.save(reminder);
        }
    }

    Instant calculateNextSendAt(String schedule) {
        try {
            LocalTime time = LocalTime.parse(schedule);
            ZonedDateTime now = ZonedDateTime.now(VN_ZONE);
            ZonedDateTime nextSend = now.with(time);
            if (nextSend.isBefore(now) || nextSend.isEqual(now)) {
                nextSend = nextSend.plusDays(1);
            }
            return nextSend.toInstant();
        } catch (Exception e) {
            return Instant.now().plusSeconds(86400); // default: tomorrow
        }
    }
}
