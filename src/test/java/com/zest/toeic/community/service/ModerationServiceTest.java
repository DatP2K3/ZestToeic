package com.zest.toeic.community.service;

import com.zest.toeic.community.repository.ModerationActionRepository;
import com.zest.toeic.shared.ai.GeminiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock private ModerationActionRepository moderationActionRepository;
    @Mock private GeminiClient geminiClient;
    @InjectMocks private ModerationService moderationService;

    @Test
    void checkContent_cleanText_returnsClean() {
        lenient().when(geminiClient.ask(anyString())).thenReturn("0.1");
        lenient().when(moderationActionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = moderationService.checkContent("Tôi muốn hỏi về Part 5", "u1");
        assertEquals("CLEAN", result);
    }

    @Test
    void checkContent_bannedWord_flagsByRegex() {
        when(moderationActionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = moderationService.checkContent("spam spam spam", "u1");
        assertEquals("REGEX_FLAGGED", result);
    }

    @Test
    void checkContent_toxicByAI_flagsCorrectly() {
        when(geminiClient.ask(anyString())).thenReturn("0.9");
        when(moderationActionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = moderationService.checkContent("Content that passes regex but is toxic", "u1");
        assertEquals("AI_FLAGGED", result);
    }

    @Test
    void checkContent_aiFailure_fallsBackClean() {
        when(geminiClient.ask(anyString())).thenThrow(new RuntimeException("API down"));

        String result = moderationService.checkContent("Normal content", "u1");
        assertEquals("CLEAN", result);
    }

    @Test
    void getStrikeCount_returnsCount() {
        when(moderationActionRepository.countByTargetIdAndAction("u1", "STRIKE")).thenReturn(3L);
        assertEquals(3L, moderationService.getStrikeCount("u1"));
    }
}
