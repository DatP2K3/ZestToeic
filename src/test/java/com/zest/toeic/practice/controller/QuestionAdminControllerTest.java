package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.config.SecurityConfig;
import com.zest.toeic.shared.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = QuestionAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class QuestionAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionRepository questionRepository;

    @Test
    @WithMockUser
    void listAll_Success() throws Exception {
        Question q = new Question();
        q.setId("q1");
        when(questionRepository.findAll()).thenReturn(List.of(q));

        mockMvc.perform(get("/api/v1/admin/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("q1"));
    }

    @Test
    @WithMockUser
    void create_Success() throws Exception {
        Question q = new Question();
        q.setContent("New Question");

        Question saved = new Question();
        saved.setId("newId");
        saved.setContent("New Question");

        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/admin/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(q)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("newId"));
    }

    @Test
    @WithMockUser
    void update_NotFound() throws Exception {
        when(questionRepository.existsById("1")).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_Success() throws Exception {
        when(questionRepository.existsById("1")).thenReturn(true);
        
        Question updated = new Question();
        updated.setId("1");
        
        when(questionRepository.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"));
    }

    @Test
    @WithMockUser
    void delete_NotFound() throws Exception {
        when(questionRepository.existsById("1")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/admin/questions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_Success() throws Exception {
        when(questionRepository.existsById("1")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/questions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void stats_Success() throws Exception {
        when(questionRepository.count()).thenReturn(100L);
        when(questionRepository.countByStatus("PUBLISHED")).thenReturn(90L);
        when(questionRepository.countByPartAndStatus(anyInt(), eq("PUBLISHED"))).thenReturn(10L);

        mockMvc.perform(get("/api/v1/admin/questions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.published").value(90))
                .andExpect(jsonPath("$.data.part1").value(10));
    }
}
