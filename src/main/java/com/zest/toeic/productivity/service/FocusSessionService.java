package com.zest.toeic.productivity.service;
import com.zest.toeic.productivity.repository.FocusSessionRepository;
import com.zest.toeic.productivity.model.FocusSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FocusSessionService {

    private final FocusSessionRepository repository;

    public FocusSessionService(FocusSessionRepository repository) {
        this.repository = repository;
    }

    public FocusSession startSession(String userId, int durationMinutes, String task) {
        // End any existing active session
        repository.findByUserIdAndEndedAtIsNull(userId).ifPresent(active -> {
            active.setEndedAt(Instant.now());
            active.setCompleted(false);
            active.setActualMinutes(Duration.between(active.getStartedAt(), active.getEndedAt()).toMinutes());
            repository.save(active);
        });

        FocusSession session = FocusSession.builder()
                .userId(userId)
                .startedAt(Instant.now())
                .durationMinutes(durationMinutes > 0 ? durationMinutes : 50)
                .taskDescription(task)
                .build();
        return repository.save(session);
    }

    public FocusSession endSession(String userId) {
        FocusSession session = repository.findByUserIdAndEndedAtIsNull(userId)
                .orElseThrow(() -> new IllegalStateException("No active session"));
        session.setEndedAt(Instant.now());
        double actual = Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes();
        session.setActualMinutes(actual);
        session.setCompleted(actual >= session.getDurationMinutes() * 0.8); // 80% of planned = completed
        return repository.save(session);
    }

    public Optional<FocusSession> getActiveSession(String userId) {
        return repository.findByUserIdAndEndedAtIsNull(userId);
    }

    public Map<String, Object> getStatistics(String userId) {
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant todayStart = LocalDate.now(vnZone).atStartOfDay(vnZone).toInstant();
        Instant weekStart = LocalDate.now(vnZone).with(DayOfWeek.MONDAY).atStartOfDay(vnZone).toInstant();
        Instant now = Instant.now();

        List<FocusSession> todaySessions = repository.findByUserIdAndStartedAtBetween(userId, todayStart, now);
        List<FocusSession> weekSessions = repository.findByUserIdAndStartedAtBetween(userId, weekStart, now);
        List<FocusSession> allCompleted = repository.findByUserIdAndCompletedTrue(userId);

        double todayMinutes = todaySessions.stream().mapToDouble(FocusSession::getActualMinutes).sum();
        double weekMinutes = weekSessions.stream().mapToDouble(FocusSession::getActualMinutes).sum();
        double longestSession = allCompleted.stream().mapToDouble(FocusSession::getActualMinutes).max().orElse(0);

        return Map.of(
                "todayMinutes", todayMinutes,
                "weekMinutes", weekMinutes,
                "longestSessionMinutes", longestSession,
                "totalSessions", allCompleted.size()
        );
    }
}
