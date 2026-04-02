package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.shared.ai.AIQuestionGenerator;
import com.zest.toeic.shared.config.SecurityConfig;
import com.zest.toeic.shared.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = QuestionGenerateController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class QuestionGenerateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIQuestionGenerator aiQuestionGenerator;

    @Test
    @WithMockUser
    void generateQuestions_Success() throws Exception {
        Map<String, Object> mockResult = Map.of(
                "status", "success",
                "count", 5
        );

        when(aiQuestionGenerator.generate(anyInt(), anyString(), anyInt())).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/admin/questions/generate")
                        .param("part", "5")
                        .param("difficulty", "MEDIUM")
                        .param("count", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.count").value(5));
    }
}
