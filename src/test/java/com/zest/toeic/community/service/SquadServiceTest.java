package com.zest.toeic.community.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.repository.SquadRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SquadServiceTest {

    @Mock
    private SquadRepository squadRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SquadService squadService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .displayName("Dat")
                .level(3)
                .totalXp(1600L)
                .build();
        mockUser.setId("user1");
    }

    // ═══ createSquad ═══

    @Test
    void createSquad_UserNotFound_ThrowsNotFound() {
        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> squadService.createSquad("user1", "Squad A"));
    }

    @Test
    void createSquad_LevelTooLow_ThrowsBadRequest() {
        mockUser.setLevel(1);
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));

        assertThrows(BadRequestException.class,
                () -> squadService.createSquad("user1", "Squad A"));
    }

    @Test
    void createSquad_EmptyName_ThrowsBadRequest() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));

        assertThrows(BadRequestException.class,
                () -> squadService.createSquad("user1", ""));
    }

    @Test
    void createSquad_NullName_ThrowsBadRequest() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));

        assertThrows(BadRequestException.class,
                () -> squadService.createSquad("user1", null));
    }

    @Test
    void createSquad_Success() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(squadRepository.save(any(Squad.class))).thenAnswer(inv -> inv.getArgument(0));

        Squad result = squadService.createSquad("user1", "Study Squad");

        assertEquals("Study Squad", result.getName());
        assertEquals("user1", result.getOwnerId());
        assertEquals(1, result.getMembers().size());
        assertEquals("user1", result.getMembers().get(0).getUserId());
    }

    // ═══ joinSquad ═══

    @Test
    void joinSquad_SquadNotFound_ThrowsNotFound() {
        when(squadRepository.findById("sq1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> squadService.joinSquad("user1", "sq1"));
    }

    @Test
    void joinSquad_SquadFull_ThrowsBadRequest() {
        Squad squad = createSquadWithMembers(5);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.joinSquad("user99", "sq1"));
    }

    @Test
    void joinSquad_AlreadyMember_ThrowsBadRequest() {
        Squad squad = createSquadWithMembers(1);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.joinSquad("member0", "sq1"));
    }

    @Test
    void joinSquad_Success() {
        Squad squad = createSquadWithMembers(1);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(squadRepository.save(any(Squad.class))).thenAnswer(inv -> inv.getArgument(0));

        Squad result = squadService.joinSquad("user1", "sq1");

        assertEquals(2, result.getMembers().size());
    }

    // ═══ leaveSquad ═══

    @Test
    void leaveSquad_OwnerCannotLeave_ThrowsBadRequest() {
        Squad squad = Squad.builder().name("Test").ownerId("user1").build();
        squad.setId("sq1");
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.leaveSquad("user1", "sq1"));
    }

    @Test
    void leaveSquad_NotMember_ThrowsBadRequest() {
        Squad squad = createSquadWithMembers(1);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.leaveSquad("user-not-in-squad", "sq1"));
    }

    @Test
    void leaveSquad_Success() {
        Squad squad = createSquadWithMembers(2);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));
        when(squadRepository.save(any(Squad.class))).thenAnswer(inv -> inv.getArgument(0));

        Squad result = squadService.leaveSquad("member1", "sq1");

        assertEquals(1, result.getMembers().size());
    }

    // ═══ kickMember ═══

    @Test
    void kickMember_NotOwner_ThrowsBadRequest() {
        Squad squad = createSquadWithMembers(2);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.kickMember("member1", "sq1", "member0"));
    }

    @Test
    void kickMember_SelfKick_ThrowsBadRequest() {
        Squad squad = createSquadWithMembers(2);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));

        assertThrows(BadRequestException.class,
                () -> squadService.kickMember("owner1", "sq1", "owner1"));
    }

    @Test
    void kickMember_Success() {
        Squad squad = createSquadWithMembers(2);
        when(squadRepository.findById("sq1")).thenReturn(Optional.of(squad));
        when(squadRepository.save(any(Squad.class))).thenAnswer(inv -> inv.getArgument(0));

        Squad result = squadService.kickMember("owner1", "sq1", "member1");

        assertEquals(1, result.getMembers().size());
    }

    // ═══ getMySquads ═══

    @Test
    void getMySquads_ReturnsSquads() {
        Squad squad = Squad.builder().name("Test").ownerId("user1").build();
        when(squadRepository.findByMemberUserId("user1")).thenReturn(List.of(squad));

        List<Squad> result = squadService.getMySquads("user1");

        assertEquals(1, result.size());
    }

    // ═══ helpers ═══

    private Squad createSquadWithMembers(int count) {
        Squad squad = Squad.builder().name("Test Squad").ownerId("owner1").build();
        squad.setId("sq1");
        for (int i = 0; i < count; i++) {
            squad.getMembers().add(Squad.SquadMember.builder()
                    .userId("member" + i).displayName("Member " + i).joinedAt(Instant.now()).build());
        }
        return squad;
    }
}
