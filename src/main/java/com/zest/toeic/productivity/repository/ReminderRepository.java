package com.zest.toeic.productivity.repository;
import com.zest.toeic.productivity.model.Reminder;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByUserId(String userId);
    List<Reminder> findByEnabledTrueAndNextSendAtBefore(Instant now);
}
