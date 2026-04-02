package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.shared.ai.AIRouter;
import com.zest.toeic.shared.ai.dto.AIExplanationResponse;
import com.zest.toeic.shared.config.SecurityConfig;
import com.zest.toeic.shared.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExplanationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class ExplanationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIRouter aiRouter;

    @Test
    @WithMockUser
    void getExplanation_Success() throws Exception {
        AIExplanationResponse mockResponse = AIExplanationResponse.builder()
                .questionId("q123")
                .userAnswer("A")
                .explanation("This is the correct choice because...")
                .provider("GEMINI")
                .cached(false)
                .build();

        when(aiRouter.explain(anyString(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/questions/q123/explanation")
                        .param("answer", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.explanation").value("This is the correct choice because..."))
                .andExpect(jsonPath("$.data.provider").value("GEMINI"));
    }
}
