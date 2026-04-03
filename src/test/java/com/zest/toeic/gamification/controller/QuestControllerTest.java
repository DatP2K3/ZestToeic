package com.zest.toeic.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.gamification.model.DailyQuest;
import com.zest.toeic.gamification.service.QuestService;
import com.zest.toeic.shared.exception.GlobalExceptionHandler;
import com.zest.toeic.shared.model.enums.QuestType;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuestControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QuestService questService;

    @InjectMocks
    private QuestController questController;

    private Authentication principal;
    private DailyQuest mockDailyQuest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(questController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");

        List<DailyQuest.Quest> quests = new ArrayList<>(List.of(
                DailyQuest.Quest.builder()
                        .type(QuestType.PRACTICE_QUESTIONS).description("Trả lời 10 câu hỏi")
                        .target(10).progress(5).completed(false).claimed(false).xpReward(50)
                        .build(),
                DailyQuest.Quest.builder()
                        .type(QuestType.REVIEW_FLASHCARDS).description("Ôn 5 flashcards")
                        .target(5).progress(5).completed(true).claimed(false).xpReward(30)
                        .build(),
                DailyQuest.Quest.builder()
                        .type(QuestType.COMPLETE_TEST).description("Hoàn thành 1 bài test")
                        .target(1).progress(0).completed(false).claimed(false).xpReward(50)
                        .build()
        ));

        mockDailyQuest = DailyQuest.builder()
                .userId("user1")
                .date(LocalDate.now())
                .quests(quests)
                .build();
        mockDailyQuest.setId("dq1");
    }

    @Test
    void getDailyQuests_Success() throws Exception {
        when(questService.getOrGenerateQuests("user1")).thenReturn(mockDailyQuest);

        mockMvc.perform(get("/api/v1/quests/daily")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value("user1"))
                .andExpect(jsonPath("$.data.quests").isArray())
                .andExpect(jsonPath("$.data.quests.length()").value(3))
                .andExpect(jsonPath("$.data.quests[0].type").value("PRACTICE_QUESTIONS"))
                .andExpect(jsonPath("$.data.quests[1].completed").value(true));
    }

    @Test
    void updateProgress_Success() throws Exception {
        when(questService.updateProgress("user1", "PRACTICE_QUESTIONS", 3)).thenReturn(mockDailyQuest);

        mockMvc.perform(post("/api/v1/quests/daily/progress")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "questType", "PRACTICE_QUESTIONS",
                                "amount", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quests[0].type").value("PRACTICE_QUESTIONS"));
    }

    @Test
    void claimReward_Success() throws Exception {
        mockDailyQuest.getQuests().get(1).setClaimed(true);
        when(questService.claimReward("user1", 1)).thenReturn(mockDailyQuest);

        mockMvc.perform(post("/api/v1/quests/daily/claim/1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quests[1].claimed").value(true));
    }
}
