package com.zest.toeic.community.service;

import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.repository.SquadRepository;
import com.zest.toeic.practice.repository.UserAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SquadStreakSchedulerTest {

    @Mock
    private SquadRepository squadRepository;

    @Mock
    private UserAnswerRepository userAnswerRepository;

    @InjectMocks
    private SquadStreakScheduler squadStreakScheduler;

    private Squad activeSquad;
    private Squad inactiveSquad;
    private Squad emptySquad;

    @BeforeEach
    void setUp() {
        // Squad where all members were active yesterday
        activeSquad = Squad.builder().name("Active Squad").ownerId("u1").streak(3).build();
        activeSquad.setId("sq1");
        activeSquad.getMembers().add(Squad.SquadMember.builder()
                .userId("u1").displayName("User 1").joinedAt(Instant.now()).build());
        activeSquad.getMembers().add(Squad.SquadMember.builder()
                .userId("u2").displayName("User 2").joinedAt(Instant.now()).build());

        // Squad where NOT all members were active
        inactiveSquad = Squad.builder().name("Inactive Squad").ownerId("u3").streak(5).build();
        inactiveSquad.setId("sq2");
        inactiveSquad.getMembers().add(Squad.SquadMember.builder()
                .userId("u3").displayName("User 3").joinedAt(Instant.now()).build());
        inactiveSquad.getMembers().add(Squad.SquadMember.builder()
                .userId("u4").displayName("User 4").joinedAt(Instant.now()).build());

        // Empty squad (should be skipped)
        emptySquad = Squad.builder().name("Empty Squad").ownerId("u5").streak(0).build();
        emptySquad.setId("sq3");
    }

    @Test
    void checkSquadStreaks_AllActive_StreakIncremented() {
        when(squadRepository.findAll()).thenReturn(List.of(activeSquad));
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u1"), any(Instant.class), any(Instant.class))).thenReturn(5L);
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u2"), any(Instant.class), any(Instant.class))).thenReturn(3L);

        squadStreakScheduler.checkSquadStreaks();

        verify(squadRepository).save(activeSquad);
        assert activeSquad.getStreak() == 4; // 3 + 1
        assert activeSquad.getLastStreakCheckDate().equals(LocalDate.now().minusDays(1));
    }

    @Test
    void checkSquadStreaks_OneInactive_StreakReset() {
        when(squadRepository.findAll()).thenReturn(List.of(inactiveSquad));
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u3"), any(Instant.class), any(Instant.class))).thenReturn(2L);
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u4"), any(Instant.class), any(Instant.class))).thenReturn(0L);

        squadStreakScheduler.checkSquadStreaks();

        verify(squadRepository).save(inactiveSquad);
        assert inactiveSquad.getStreak() == 0; // reset from 5 to 0
    }

    @Test
    void checkSquadStreaks_EmptySquad_Skipped() {
        when(squadRepository.findAll()).thenReturn(List.of(emptySquad));

        squadStreakScheduler.checkSquadStreaks();

        // Empty squad is skipped, but still saved with lastStreakCheckDate
        verify(userAnswerRepository, never()).countByUserIdAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void checkSquadStreaks_MixedSquads_ProcessedCorrectly() {
        when(squadRepository.findAll()).thenReturn(List.of(activeSquad, inactiveSquad, emptySquad));
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u1"), any(Instant.class), any(Instant.class))).thenReturn(1L);
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u2"), any(Instant.class), any(Instant.class))).thenReturn(1L);
        when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u3"), any(Instant.class), any(Instant.class))).thenReturn(0L);
        // u4 may not be called due to allMatch short-circuit when u3 returns 0
        lenient().when(userAnswerRepository.countByUserIdAndCreatedAtBetween(eq("u4"), any(Instant.class), any(Instant.class))).thenReturn(0L);

        squadStreakScheduler.checkSquadStreaks();

        assert activeSquad.getStreak() == 4;     // incremented
        assert inactiveSquad.getStreak() == 0;    // reset
        verify(squadRepository, times(2)).save(any(Squad.class)); // empty squad skipped by continue
    }
}
