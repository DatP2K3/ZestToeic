package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestAnswerResponse;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.service.TestService;
import com.zest.toeic.shared.model.enums.TestType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TestService testService;

    @InjectMocks
    private TestController testController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");
    }

    @Test
    void startPlacementTest_Success() throws Exception {
        TestSession session = TestSession.builder().type(TestType.PLACEMENT).build();
        session.setId("s1");
        
        when(testService.startPlacementTest("user1")).thenReturn(session);

        mockMvc.perform(post("/api/v1/tests/placement/start")
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("s1"))
                .andExpect(jsonPath("$.data.type").value("PLACEMENT"));
    }

    @Test
    void startMiniTest_Success() throws Exception {
        TestSession session = TestSession.builder().type(TestType.MINI).build();
        session.setId("s1");

        when(testService.startMiniTest(eq("user1"), any(StartTestRequest.class))).thenReturn(session);

        StartTestRequest req = new StartTestRequest();
        req.setPart(5);
        req.setQuestionCount(10);
        req.setTimeLimitMinutes(15);

        mockMvc.perform(post("/api/v1/tests/mini/start")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("MINI"));
    }

    @Test
    void submitAnswer_Success() throws Exception {
        TestAnswerRequest req = new TestAnswerRequest();
        req.setQuestionId("q1");
        req.setSelectedOption("A");

        TestAnswerResponse response = TestAnswerResponse.builder()
                .correct(true)
                .xpEarned(10)
                .build();

        when(testService.submitTestAnswer(eq("user1"), eq("s1"), any(TestAnswerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/tests/s1/answer")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.xpEarned").value(10));
    }

    @Test
    void completeTest_Success() throws Exception {
        TestResult result = TestResult.builder().correctCount(20).build();
        when(testService.completeTest("user1", "s1")).thenReturn(result);

        mockMvc.perform(post("/api/v1/tests/s1/complete")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correctCount").value(20));
    }

    @Test
    void getTest_Success() throws Exception {
        TestSession session = TestSession.builder().type(TestType.MOCK).build();
        session.setId("s1");
        when(testService.getTestSession("user1", "s1")).thenReturn(session);

        mockMvc.perform(get("/api/v1/tests/s1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("s1"));
    }

    @Test
    void completeTest_AlreadyCompleted_ThrowsException() throws Exception {
        when(testService.completeTest("user1", "s1"))
                .thenThrow(new com.zest.toeic.shared.exception.BadRequestException("Test đã hoàn thành trước đó"));

        mockMvc.perform(post("/api/v1/tests/s1/complete")
                        .principal(principal))
                .andExpect(status().isBadRequest());
    }
}
