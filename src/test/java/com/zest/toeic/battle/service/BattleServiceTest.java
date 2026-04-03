package com.zest.toeic.battle.service;
import com.zest.toeic.battle.repository.BattleParticipantRepository;
import com.zest.toeic.battle.repository.BattleRepository;
import com.zest.toeic.battle.model.BattleParticipant;
import com.zest.toeic.battle.model.Battle;

import com.zest.toeic.notification.service.NotificationService;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.BattleStatus;
import com.zest.toeic.shared.model.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock private BattleRepository battleRepository;
    @Mock private BattleParticipantRepository participantRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private BattleService service;

    @Test
    void scheduleBattle() {
        Battle battle = Battle.builder().title("Test Battle").build();
        when(battleRepository.save(any())).thenReturn(battle);
        Battle result = service.scheduleBattle(battle);
        assertNotNull(result);
        assertEquals(BattleStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void register_success() {
        Battle battle = Battle.builder().status(BattleStatus.SCHEDULED).maxPlayers(30).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(participantRepository.countByBattleId("b1")).thenReturn(5L);
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.empty());
        when(participantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BattleParticipant p = service.register("b1", "u1", "User 1");
        assertEquals("u1", p.getUserId());
    }

    @Test
    void register_battleFull() {
        Battle battle = Battle.builder().status(BattleStatus.SCHEDULED).maxPlayers(2).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(participantRepository.countByBattleId("b1")).thenReturn(2L);
        assertThrows(IllegalStateException.class, () -> service.register("b1", "u1", "User 1"));
    }

    @Test
    void register_alreadyRegistered() {
        Battle battle = Battle.builder().status(BattleStatus.SCHEDULED).maxPlayers(30).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(participantRepository.countByBattleId("b1")).thenReturn(1L);
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.of(new BattleParticipant()));
        assertThrows(IllegalStateException.class, () -> service.register("b1", "u1", "User 1"));
    }

    @Test
    void register_battleNotOpen() {
        Battle battle = Battle.builder().status(BattleStatus.COMPLETED).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        assertThrows(IllegalStateException.class, () -> service.register("b1", "u1", "User 1"));
    }

    @Test
    void startBattle() {
        Battle battle = Battle.builder().status(BattleStatus.SCHEDULED).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(battleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Battle result = service.startBattle("b1");
        assertEquals(BattleStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartedAt());
    }

    @Test
    void submitAnswer_correct_fast() {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("u1").score(0).correctCount(0).build();
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.of(p));
        when(participantRepository.save(any())).thenReturn(p);

        int score = service.submitAnswer("b1", "u1", "q1", "A", true, 5000);
        assertEquals(15, score); // 10 + 5 speed bonus (< 10s)
    }

    @Test
    void submitAnswer_correct_medium() {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("u1").score(0).correctCount(0).build();
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.of(p));
        when(participantRepository.save(any())).thenReturn(p);

        int score = service.submitAnswer("b1", "u1", "q1", "A", true, 15000);
        assertEquals(13, score); // 10 + 3
    }

    @Test
    void submitAnswer_correct_slow() {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("u1").score(0).correctCount(0).build();
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.of(p));
        when(participantRepository.save(any())).thenReturn(p);

        int score = service.submitAnswer("b1", "u1", "q1", "A", true, 25000);
        assertEquals(11, score); // 10 + 1
    }

    @Test
    void submitAnswer_wrong() {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("u1").score(10).correctCount(1).build();
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.of(p));
        when(participantRepository.save(any())).thenReturn(p);

        int score = service.submitAnswer("b1", "u1", "q1", "B", false, 5000);
        assertEquals(-1, score);
    }

    @Test
    void submitAnswer_participantNotFound() {
        when(participantRepository.findByBattleIdAndUserId("b1", "u1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.submitAnswer("b1", "u1", "q1", "A", true, 5000));
    }

    @Test
    void endBattle() {
        Battle battle = Battle.builder().status(BattleStatus.IN_PROGRESS).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(battleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BattleParticipant p1 = BattleParticipant.builder().battleId("b1").userId("u1").score(50).build();
        BattleParticipant p2 = BattleParticipant.builder().battleId("b1").userId("u2").score(30).build();
        when(participantRepository.findByBattleIdOrderByScoreDesc("b1")).thenReturn(List.of(p1, p2));
        when(participantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(notificationService.send(anyString(), any(NotificationType.class), anyString(), anyString(), any())).thenReturn(null);

        Battle result = service.endBattle("b1");
        assertEquals(BattleStatus.COMPLETED, result.getStatus());
        assertEquals(1, p1.getRank());
        assertEquals(2, p2.getRank());
    }

    @Test
    void getBattle_notFound() {
        when(battleRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getBattle("x"));
    }

    @Test
    void getActiveBattles() {
        when(battleRepository.findByStatusInOrderByScheduledAtDesc(any())).thenReturn(List.of());
        assertEquals(0, service.getActiveBattles().size());
    }

    @Test
    void nextQuestion() {
        Battle battle = Battle.builder().currentQuestionIndex(2).build();
        battle.setId("b1");
        when(battleRepository.findById("b1")).thenReturn(Optional.of(battle));
        when(battleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Battle result = service.nextQuestion("b1");
        assertEquals(3, result.getCurrentQuestionIndex());
    }

    @Test
    void getResults() {
        when(participantRepository.findByBattleIdOrderByScoreDesc("b1")).thenReturn(List.of());
        assertEquals(0, service.getResults("b1").size());
    }
}
