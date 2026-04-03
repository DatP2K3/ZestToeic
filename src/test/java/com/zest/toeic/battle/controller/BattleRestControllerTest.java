package com.zest.toeic.battle.controller;
import com.zest.toeic.battle.model.BattleParticipant;
import com.zest.toeic.battle.model.Battle;
import com.zest.toeic.battle.service.BattleService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zest.toeic.shared.model.enums.BattleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BattleRestControllerTest {

    @Mock private BattleService battleService;
    @Mock private BattleWebSocketController wsController;
    @InjectMocks private BattleRestController controller;
    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("user1", null, List.of());

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void listBattles() throws Exception {
        when(battleService.getActiveBattles()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/battles")).andExpect(status().isOk());
    }

    @Test
    void getBattle() throws Exception {
        Battle b = Battle.builder().title("Test").status(BattleStatus.SCHEDULED).build();
        b.setId("b1");
        when(battleService.getBattle("b1")).thenReturn(b);
        mockMvc.perform(get("/api/v1/battles/b1")).andExpect(status().isOk());
    }

    @Test
    void register_broadcastsPlayerJoined() throws Exception {
        BattleParticipant p = BattleParticipant.builder().battleId("b1").userId("user1").build();
        when(battleService.register(eq("b1"), anyString(), anyString())).thenReturn(p);
        mockMvc.perform(post("/api/v1/battles/b1/register").principal(auth))
                .andExpect(status().isCreated());
        verify(wsController).broadcastPlayerJoined("b1", p);
    }

    @Test
    void getResults() throws Exception {
        when(battleService.getResults("b1")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/battles/b1/results")).andExpect(status().isOk());
    }

    @Test
    void scheduleBattle() throws Exception {
        Battle b = Battle.builder().title("Weekly").scheduledAt(Instant.now()).build();
        when(battleService.scheduleBattle(any())).thenReturn(b);
        mockMvc.perform(post("/api/v1/admin/battles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(b)))
                .andExpect(status().isCreated());
    }

    @Test
    void startBattle_broadcastsStart() throws Exception {
        Battle b = Battle.builder().status(BattleStatus.IN_PROGRESS).build();
        when(battleService.startBattle("b1")).thenReturn(b);
        mockMvc.perform(post("/api/v1/admin/battles/b1/start")).andExpect(status().isOk());
        verify(wsController).broadcastStart("b1");
    }

    @Test
    void endBattle_broadcastsEnd() throws Exception {
        Battle b = Battle.builder().status(BattleStatus.COMPLETED).build();
        when(battleService.endBattle("b1")).thenReturn(b);
        mockMvc.perform(post("/api/v1/admin/battles/b1/end")).andExpect(status().isOk());
        verify(wsController).broadcastEnd("b1");
    }
}
