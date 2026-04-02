package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.model.Badge;
import com.zest.toeic.gamification.model.UserBadge;
import com.zest.toeic.gamification.model.UserStreak;
import com.zest.toeic.gamification.repository.BadgeRepository;
import com.zest.toeic.gamification.repository.UserBadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @InjectMocks private BadgeService badgeService;

    @Test
    void getAllBadges_returnsActiveBadges() {
        Badge b = Badge.builder().name("Test").build();
        when(badgeRepository.findByActiveTrue()).thenReturn(List.of(b));
        assertEquals(1, badgeService.getAllBadges().size());
    }

    @Test
    void getUserBadges_returnsList() {
        when(userBadgeRepository.findByUserId("u1")).thenReturn(List.of());
        assertTrue(badgeService.getUserBadges("u1").isEmpty());
    }

    @Test
    void checkAndAwardForStreak_awardsStreak7() {
        UserStreak streak = UserStreak.builder().currentStreak(7).build();
        Badge badge = Badge.builder().name("Streak 7").criteria("STREAK_7").build();
        badge.setId("b1");

        when(userBadgeRepository.existsByUserIdAndBadgeId("u1", "STREAK_7")).thenReturn(false);
        when(badgeRepository.findByCriteria("STREAK_7")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        badgeService.checkAndAwardForStreak("u1", streak);
        verify(userBadgeRepository, atLeastOnce()).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardForStreak_skipsIfAlreadyEarned() {
        UserStreak streak = UserStreak.builder().currentStreak(7).build();
        when(userBadgeRepository.existsByUserIdAndBadgeId("u1", "STREAK_7")).thenReturn(true);

        badgeService.checkAndAwardForStreak("u1", streak);
        verify(badgeRepository, never()).findByCriteria("STREAK_7");
    }

    @Test
    void checkAndAwardForLevel_awardsLevel5() {
        Badge badge = Badge.builder().name("Level 5").criteria("LEVEL_5").build();
        badge.setId("b2");
        when(userBadgeRepository.existsByUserIdAndBadgeId("u1", "LEVEL_5")).thenReturn(false);
        when(badgeRepository.findByCriteria("LEVEL_5")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        badgeService.checkAndAwardForLevel("u1", 5);
        verify(userBadgeRepository, atLeastOnce()).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardForQuestions_awards100() {
        Badge badge = Badge.builder().name("100 Questions").criteria("QUESTIONS_100").build();
        badge.setId("b3");
        when(userBadgeRepository.existsByUserIdAndBadgeId("u1", "QUESTIONS_100")).thenReturn(false);
        when(badgeRepository.findByCriteria("QUESTIONS_100")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        badgeService.checkAndAwardForQuestions("u1", 100);
        verify(userBadgeRepository, atLeastOnce()).save(any(UserBadge.class));
    }

    @Test
    void initDefaultBadges_skipsIfNotEmpty() {
        when(badgeRepository.count()).thenReturn(10L);
        badgeService.initDefaultBadges();
        verify(badgeRepository, never()).saveAll(any());
    }

    @Test
    void initDefaultBadges_createsDefaultsWhenEmpty() {
        when(badgeRepository.count()).thenReturn(0L);
        when(badgeRepository.saveAll(any())).thenReturn(List.of());
        badgeService.initDefaultBadges();
        verify(badgeRepository).saveAll(any());
    }
}
