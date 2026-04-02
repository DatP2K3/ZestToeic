package com.zest.toeic.gamification.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.gamification.dto.LevelUpResponse;
import com.zest.toeic.gamification.dto.XpSummary;
import com.zest.toeic.gamification.model.XpTransaction;
import com.zest.toeic.gamification.repository.XpTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private XpTransactionRepository xpRepo;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .level(1)
                .totalXp(0L)
                .build();
        mockUser.setId("u1");
    }

    @Test
    void awardXp_UserNotFound_ReturnsNull() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        LevelUpResponse response = gamificationService.awardXp("nonexistent", 100, "TEST", "t1", "desc");

        assertNull(response);
    }

    @Test
    void awardXp_NormalAmount_BelowCap_NoLevelUp() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(xpRepo.findByUserIdAndCreatedAtAfter(eq("u1"), any(Instant.class))).thenReturn(List.of());

        LevelUpResponse response = gamificationService.awardXp("u1", 100, "TEST", "t1", "desc");

        assertNotNull(response);
        assertEquals(1, response.getOldLevel());
        assertEquals(1, response.getNewLevel()); // 100 XP < 500
        assertEquals(100L, response.getTotalXp());
        assertFalse(response.isLeveledUp());

        verify(userRepository).save(mockUser);
        verify(xpRepo).save(any(XpTransaction.class));
    }

    @Test
    void awardXp_AboveCap_AppliesSoftCap() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        // Simulate already having 450 XP today
        XpTransaction txContext = XpTransaction.builder().amount(450).build();
        when(xpRepo.findByUserIdAndCreatedAtAfter(eq("u1"), any(Instant.class))).thenReturn(List.of(txContext));

        // Attempting to give 100 XP.
        // Cap is 500. So 50 XP is full, remaining 50 XP is reduced to 50% = 25 XP.
        // Total given: 75 XP. Total: 75 XP in this transaction.

        LevelUpResponse response = gamificationService.awardXp("u1", 100, "TEST", "t1", "desc");

        assertNotNull(response);
        assertEquals(75L, response.getTotalXp()); // 0+75 total since mockUser starts at 0 total
    }

    @Test
    void awardXp_AlreadyOverCap_AppliesSoftCapFully() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        // Simulate already having 500 XP today
        XpTransaction txContext = XpTransaction.builder().amount(500).build();
        when(xpRepo.findByUserIdAndCreatedAtAfter(eq("u1"), any(Instant.class))).thenReturn(List.of(txContext));

        // Giving 100 XP over cap -> 50% -> 50 XP added.
        LevelUpResponse response = gamificationService.awardXp("u1", 100, "TEST", "t1", "desc");

        assertNotNull(response);
        assertEquals(50L, response.getTotalXp());
    }

    @Test
    void awardXp_CausesLevelUp() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(xpRepo.findByUserIdAndCreatedAtAfter(eq("u1"), any(Instant.class))).thenReturn(List.of());

        // Level 2 threshold is 500. Let's give 500 XP.
        LevelUpResponse response = gamificationService.awardXp("u1", 500, "TEST", "t1", "desc");

        assertNotNull(response);
        assertEquals(1, response.getOldLevel());
        assertEquals(2, response.getNewLevel());
        assertEquals(500L, response.getTotalXp());
        assertTrue(response.isLeveledUp());
    }

    @Test
    void getXpSummary_UserNotFound_ReturnsNull() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        XpSummary summary = gamificationService.getXpSummary("u1");

        assertNull(summary);
    }

    @Test
    void getXpSummary_CalculatesCorrectly() {
        mockUser.setTotalXp(600L); // Level 2
        mockUser.setLevel(2);
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));

        XpTransaction txContext = XpTransaction.builder().amount(150).build();
        when(xpRepo.findByUserIdAndCreatedAtAfter(eq("u1"), any(Instant.class))).thenReturn(List.of(txContext));

        XpSummary summary = gamificationService.getXpSummary("u1");

        assertNotNull(summary);
        assertEquals(600L, summary.getTotalXp());
        assertEquals(2, summary.getLevel());
        assertEquals(500L, summary.getXpForCurrentLevel());
        assertEquals(1500L, summary.getXpForNextLevel()); // 1500 for level 3
        assertEquals(100L, summary.getXpProgress()); // 600 - 500 = 100
        assertEquals(150, summary.getTodayXp());
        assertEquals(500, summary.getDailyCap());
        assertFalse(summary.isCapReached());
        assertEquals(10.0, summary.getProgressPercent()); // 100 / 1000 = 10%
    }

    @Test
    void getXpHistory_ReturnsPage() {
        Page<XpTransaction> pageMock = new PageImpl<>(List.of(new XpTransaction()));
        when(xpRepo.findByUserIdOrderByCreatedAtDesc("u1", PageRequest.of(0, 10))).thenReturn(pageMock);

        Page<XpTransaction> result = gamificationService.getXpHistory("u1", 0, 10);

        assertEquals(1, result.getContent().size());
    }
}
