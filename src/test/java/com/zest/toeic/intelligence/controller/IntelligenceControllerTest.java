package com.zest.toeic.intelligence.controller;

import com.zest.toeic.intelligence.dto.RecommendationResponse;
import com.zest.toeic.intelligence.dto.ScorePredictionResponse;
import com.zest.toeic.intelligence.service.IntelligenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IntelligenceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IntelligenceService intelligenceService;

    @InjectMocks
    private IntelligenceController intelligenceController;

    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(intelligenceController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("u1", "auth");
    }

    @Test
    void getRecommendations_returnsOk() throws Exception {
        RecommendationResponse res = RecommendationResponse.builder().build();
        when(intelligenceService.getRecommendations(anyString())).thenReturn(res);

        mockMvc.perform(get("/api/v1/intelligence/recommendations")
                .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void predictScore_returnsOk() throws Exception {
        ScorePredictionResponse res = ScorePredictionResponse.builder().predictedScore(500).build();
        when(intelligenceService.predictScore(anyString())).thenReturn(res);

        mockMvc.perform(get("/api/v1/intelligence/score-prediction")
                .principal(principal))
                .andExpect(status().isOk());
    }
}
