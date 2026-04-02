package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuestionAdminControllerTest {

    private MockMvc mockMvc;

    @Mock private QuestionRepository questionRepository;
    @InjectMocks private QuestionAdminController questionAdminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(questionAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deleteQuestion_ReturnsSuccess() throws Exception {
        when(questionRepository.existsById("q1")).thenReturn(true);
        mockMvc.perform(delete("/api/v1/admin/questions/q1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteQuestion_NotFound() throws Exception {
        when(questionRepository.existsById("q1")).thenReturn(false);
        mockMvc.perform(delete("/api/v1/admin/questions/q1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAll_ReturnsOk() throws Exception {
        when(questionRepository.findAll()).thenReturn(List.of(new Question()));
        mockMvc.perform(get("/api/v1/admin/questions"))
                .andExpect(status().isOk());
    }

    @Test
    void create_ReturnsCreated() throws Exception {
        when(questionRepository.save(any(Question.class))).thenReturn(new Question());
        mockMvc.perform(post("/api/v1/admin/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void update_ReturnsOk() throws Exception {
        when(questionRepository.existsById("q1")).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenReturn(new Question());
        mockMvc.perform(put("/api/v1/admin/questions/q1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }
}
