package com.zest.toeic.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.gamification.dto.LeaderboardEntry;
import com.zest.toeic.gamification.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaderboardController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void getGlobalLeaderboard_returnsList() throws Exception {
        LeaderboardEntry res = LeaderboardEntry.builder().userId("u1").displayName("Test").build();
        when(leaderboardService.getLeaderboard(anyString(), anyInt(), anyInt())).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/leaderboards")
                .param("period", "WEEKLY"))
                .andExpect(status().isOk());
    }
}
