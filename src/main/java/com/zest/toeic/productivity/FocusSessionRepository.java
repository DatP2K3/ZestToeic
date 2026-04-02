package com.zest.toeic.productivity;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FocusSessionRepository extends MongoRepository<FocusSession, String> {
    Optional<FocusSession> findByUserIdAndEndedAtIsNull(String userId);
    List<FocusSession> findByUserIdAndStartedAtBetween(String userId, Instant start, Instant end);
    List<FocusSession> findByUserIdAndCompletedTrue(String userId);
}
