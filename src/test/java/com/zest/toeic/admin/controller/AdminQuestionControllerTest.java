package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.service.AdminQuestionService;
import com.zest.toeic.practice.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminQuestionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminQuestionService adminQuestionService;

    @InjectMocks
    private AdminQuestionController adminQuestionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminQuestionController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void getQuestions_returnsPage() throws Exception {
        when(adminQuestionService.listQuestions(any(), any(), anyInt(), anyInt())).thenReturn(null);
        
        mockMvc.perform(get("/api/v1/admin/questions"))
                .andExpect(status().isOk());
    }

    @Test
    void addQuestion_returnsCreated() throws Exception {
        Question q = Question.builder().content("c").build();
        when(adminQuestionService.createQuestion(any(Question.class))).thenReturn(q);

        mockMvc.perform(post("/api/v1/admin/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuestion_returnsOk() throws Exception {
        Question q = Question.builder().build();
        when(adminQuestionService.updateQuestion(anyString(), any(Question.class))).thenReturn(q);

        mockMvc.perform(put("/api/v1/admin/questions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteQuestion_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/questions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void approveQuestion_returnsOk() throws Exception {
        when(adminQuestionService.approveQuestion(anyString())).thenReturn(Question.builder().build());
        mockMvc.perform(put("/api/v1/admin/questions/1/approve"))
                .andExpect(status().isOk());
    }
}
