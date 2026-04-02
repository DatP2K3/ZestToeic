package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.practice.dto.CreateFlashcardRequest;
import com.zest.toeic.practice.dto.FlashcardStats;
import com.zest.toeic.practice.model.Flashcard;
import com.zest.toeic.practice.service.FlashcardService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FlashcardService flashcardService;

    @InjectMocks
    private FlashcardController flashcardController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flashcardController).build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");
    }

    @Test
    void create_ReturnsCreated() throws Exception {
        CreateFlashcardRequest req = new CreateFlashcardRequest();
        req.setFront("word");
        req.setBack("meaning");

        Flashcard card = Flashcard.builder().front("word").build();
        card.setId("c1");
        
        when(flashcardService.createFlashcard(eq("user1"), any(CreateFlashcardRequest.class))).thenReturn(card);

        mockMvc.perform(post("/api/v1/flashcards")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.front").value("word"));
    }

    @Test
    void getDueCards_ReturnsList() throws Exception {
        Flashcard card = Flashcard.builder().front("word").build();
        card.setId("c1");
        
        when(flashcardService.getDueCards("user1", 20)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/flashcards/due")
                        .principal(principal)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value("c1"));
    }

    @Test
    void review_Success() throws Exception {
        Flashcard card = Flashcard.builder().easeFactor(2.6).build();
        card.setId("c1");

        when(flashcardService.reviewCard("user1", "c1", 4)).thenReturn(card);

        mockMvc.perform(post("/api/v1/flashcards/c1/review")
                        .principal(principal)
                        .param("quality", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.easeFactor").value(2.6));
    }

    @Test
    void getStats_Success() throws Exception {
        FlashcardStats stats = FlashcardStats.builder().total(100L).build();
        when(flashcardService.getStats("user1")).thenReturn(stats);

        mockMvc.perform(get("/api/v1/flashcards/stats")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(100));
    }

    @Test
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/flashcards/c1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(flashcardService).deleteFlashcard("user1", "c1");
    }
}
