package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.practice.dto.AnswerHistoryResponse;
import com.zest.toeic.practice.dto.AnswerResult;
import com.zest.toeic.practice.dto.SubmitAnswerRequest;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.service.PracticeService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PracticeService practiceService;

    @InjectMocks
    private PracticeController practiceController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(practiceController).build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");
    }

    @Test
    void getRandomQuestions_ReturnsList() throws Exception {
        Question q = Question.builder().part(5).category("GRAMMAR").build();
        q.setId("q1");
        
        when(practiceService.getRandomQuestions(5, "EASY", 10)).thenReturn(List.of(q));

        mockMvc.perform(get("/api/v1/questions/random")
                        .param("part", "5")
                        .param("difficulty", "EASY")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value("q1"));
    }

    @Test
    void getQuestion_ReturnsQuestion() throws Exception {
        Question q = Question.builder().part(5).build();
        q.setId("q1");
        when(practiceService.getQuestionById("q1")).thenReturn(q);

        mockMvc.perform(get("/api/v1/questions/q1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("q1"));
    }

    @Test
    void submitAnswer_Success() throws Exception {
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setQuestionId("q1");
        request.setSelectedOption("A");
        request.setTimeTaken(15);

        AnswerResult result = AnswerResult.builder()
                .questionId("q1")
                .correct(true)
                .xpEarned(10)
                .build();

        // using standalone setup we pass principal to MockMvcRequestBuilders.principal()
        when(practiceService.submitAnswer(eq("user1"), any(SubmitAnswerRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/answers/submit")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.xpEarned").value(10));
    }

    @Test
    void getAnswerHistory_ReturnsHistory() throws Exception {
        AnswerHistoryResponse response = AnswerHistoryResponse.builder()
                .totalAnswers(10L)
                .correctCount(8L)
                .accuracy(80.0)
                .build();

        when(practiceService.getAnswerHistory("user1", 0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/v1/answers/history")
                        .principal(principal)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAnswers").value(10))
                .andExpect(jsonPath("$.data.accuracy").value(80.0));
    }
}
