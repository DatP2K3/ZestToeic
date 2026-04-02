package com.zest.toeic.productivity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FocusSessionControllerTest {

    @Mock private FocusSessionService focusSessionService;
    @InjectMocks private FocusSessionController controller;
    private MockMvc mockMvc;
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("user1", null, List.of());

    @BeforeEach
    void setup() { mockMvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void start() throws Exception {
        FocusSession s = FocusSession.builder().userId("user1").durationMinutes(50).build();
        when(focusSessionService.startSession(anyString(), anyInt(), any())).thenReturn(s);
        mockMvc.perform(post("/api/v1/focus/start").principal(auth)).andExpect(status().isCreated());
    }

    @Test
    void stop() throws Exception {
        FocusSession s = FocusSession.builder().userId("user1").completed(true).build();
        when(focusSessionService.endSession(anyString())).thenReturn(s);
        mockMvc.perform(post("/api/v1/focus/stop").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void current_exists() throws Exception {
        FocusSession s = FocusSession.builder().userId("user1").build();
        when(focusSessionService.getActiveSession("user1")).thenReturn(Optional.of(s));
        mockMvc.perform(get("/api/v1/focus/current").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void current_empty() throws Exception {
        when(focusSessionService.getActiveSession("user1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/focus/current").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void stats() throws Exception {
        when(focusSessionService.getStatistics("user1")).thenReturn(Map.of("todayMinutes", 30.0));
        mockMvc.perform(get("/api/v1/focus/stats").principal(auth)).andExpect(status().isOk());
    }
}
