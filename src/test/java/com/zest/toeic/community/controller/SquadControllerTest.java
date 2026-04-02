package com.zest.toeic.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.service.SquadService;
import com.zest.toeic.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SquadControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SquadService squadService;

    @InjectMocks
    private SquadController squadController;

    private Authentication principal;
    private Squad mockSquad;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(squadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");

        mockSquad = Squad.builder()
                .name("Study Squad")
                .ownerId("user1")
                .build();
        mockSquad.setId("squad1");
        mockSquad.getMembers().add(Squad.SquadMember.builder()
                .userId("user1").displayName("Dat").joinedAt(Instant.now()).build());
    }

    @Test
    void createSquad_Success() throws Exception {
        when(squadService.createSquad("user1", "Study Squad")).thenReturn(mockSquad);

        mockMvc.perform(post("/api/v1/squads")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Study Squad"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.name").value("Study Squad"))
                .andExpect(jsonPath("$.data.ownerId").value("user1"));
    }

    @Test
    void joinSquad_Success() throws Exception {
        when(squadService.joinSquad("user1", "squad1")).thenReturn(mockSquad);

        mockMvc.perform(post("/api/v1/squads/squad1/join")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Study Squad"));
    }

    @Test
    void leaveSquad_Success() throws Exception {
        when(squadService.leaveSquad("user1", "squad1")).thenReturn(mockSquad);

        mockMvc.perform(post("/api/v1/squads/squad1/leave")
                        .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void kickMember_Success() throws Exception {
        when(squadService.kickMember("user1", "squad1", "user2")).thenReturn(mockSquad);

        mockMvc.perform(delete("/api/v1/squads/squad1/members/user2")
                        .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void getSquadDetails_Success() throws Exception {
        when(squadService.getSquadDetails("squad1")).thenReturn(mockSquad);

        mockMvc.perform(get("/api/v1/squads/squad1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Study Squad"));
    }

    @Test
    void getMySquads_Success() throws Exception {
        when(squadService.getMySquads("user1")).thenReturn(List.of(mockSquad));

        mockMvc.perform(get("/api/v1/squads/my")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Study Squad"));
    }
}
