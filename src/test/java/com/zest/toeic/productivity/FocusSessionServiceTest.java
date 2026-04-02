package com.zest.toeic.productivity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FocusSessionServiceTest {

    @Mock private FocusSessionRepository repository;
    @InjectMocks private FocusSessionService service;

    @Test
    void startSession_noActive() {
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        FocusSession result = service.startSession("u1", 50, "Study vocab");
        assertNotNull(result);
        assertEquals(50, result.getDurationMinutes());
        assertEquals("Study vocab", result.getTaskDescription());
    }

    @Test
    void startSession_endsExisting() {
        FocusSession active = FocusSession.builder().userId("u1").startedAt(Instant.now().minusSeconds(300)).build();
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.of(active));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.startSession("u1", 25, "New task");
        assertNotNull(active.getEndedAt());
        verify(repository, times(2)).save(any());
    }

    @Test
    void startSession_defaultDuration() {
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        FocusSession result = service.startSession("u1", 0, null);
        assertEquals(50, result.getDurationMinutes());
    }

    @Test
    void endSession_success() {
        FocusSession session = FocusSession.builder()
                .userId("u1").startedAt(Instant.now().minusSeconds(3000)).durationMinutes(50).build();
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.of(session));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        FocusSession result = service.endSession("u1");
        assertNotNull(result.getEndedAt());
    }

    @Test
    void endSession_noActive() {
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> service.endSession("u1"));
    }

    @Test
    void getActiveSession() {
        FocusSession s = FocusSession.builder().userId("u1").build();
        when(repository.findByUserIdAndEndedAtIsNull("u1")).thenReturn(Optional.of(s));
        assertTrue(service.getActiveSession("u1").isPresent());
    }

    @Test
    void getStatistics() {
        FocusSession s1 = FocusSession.builder().actualMinutes(45).build();
        when(repository.findByUserIdAndStartedAtBetween(eq("u1"), any(), any())).thenReturn(List.of(s1));
        when(repository.findByUserIdAndCompletedTrue("u1")).thenReturn(List.of(s1));

        Map<String, Object> stats = service.getStatistics("u1");
        assertNotNull(stats);
        assertTrue(stats.containsKey("todayMinutes"));
        assertTrue(stats.containsKey("weekMinutes"));
        assertTrue(stats.containsKey("longestSessionMinutes"));
        assertTrue(stats.containsKey("totalSessions"));
    }
}
