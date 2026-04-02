package com.zest.toeic.monetization.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.monetization.model.Subscription;
import com.zest.toeic.monetization.repository.SubscriptionRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VNPayService vnPayService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User mockUser;
    private Subscription activePremium;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().subscriptionTier("FREE").build();
        mockUser.setId("u1");
        activePremium = Subscription.builder()
                .userId("u1")
                .plan("PREMIUM")
                .status("ACTIVE")
                .endDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void getCurrentSubscription_shouldReturnActiveOrFreeDefault() {
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1"))
                .thenReturn(Optional.of(activePremium));

        Subscription result = subscriptionService.getCurrentSubscription("u1");
        assertEquals("PREMIUM", result.getPlan());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void getCurrentSubscription_shouldReturnFreeIfNone() {
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1"))
                .thenReturn(Optional.empty());

        Subscription result = subscriptionService.getCurrentSubscription("u1");
        assertEquals("FREE", result.getPlan());
    }

    @Test
    void initiatePremiumUpgrade_shouldCreateVNPayUrl() {
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1")).thenReturn(Optional.empty());
        when(vnPayService.createPaymentUrl(eq("u1"), anyLong(), anyString(), anyString()))
                .thenReturn("http://vnpay.com/pay");

        String url = subscriptionService.initiatePremiumUpgrade("u1", "127.0.0.1");
        assertEquals("http://vnpay.com/pay", url);
    }

    @Test
    void initiatePremiumUpgrade_shouldThrowIfAlreadyPremium() {
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1")).thenReturn(Optional.of(activePremium));

        assertThrows(BadRequestException.class, () ->
                subscriptionService.initiatePremiumUpgrade("u1", "127.0.0.1"));
    }

    @Test
    void activatePremium_shouldUpdateUserAndCreateSubscription() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription sub = subscriptionService.activatePremium("u1", "txn123");

        assertEquals("PREMIUM", sub.getPlan());
        assertEquals("ACTIVE", sub.getStatus());
        assertEquals("VNPAY", sub.getPaymentMethod());
        assertEquals("txn123", sub.getLastTransactionId());
        
        verify(userRepository).save(mockUser);
        assertEquals("PREMIUM", mockUser.getSubscriptionTier());
    }

    @Test
    void cancelSubscription_shouldSetCancelledAndAutoRenewFalse() {
        when(subscriptionRepository.findByUserIdAndStatus("u1", "ACTIVE"))
                .thenReturn(Optional.of(activePremium));
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription sub = subscriptionService.cancelSubscription("u1");

        assertFalse(sub.isAutoRenew());
        assertEquals("CANCELLED", sub.getStatus());
        assertEquals("FREE", mockUser.getSubscriptionTier());
        verify(userRepository).save(mockUser);
    }

    @Test
    void cancelSubscription_shouldThrowIfNoActiveSub() {
        when(subscriptionRepository.findByUserIdAndStatus("u1", "ACTIVE")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> subscriptionService.cancelSubscription("u1"));
    }

    @Test
    void isPremium_shouldReturnTrueIfActivePremium() {
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1")).thenReturn(Optional.of(activePremium));

        assertTrue(subscriptionService.isPremium("u1"));
    }

    @Test
    void isPremium_shouldReturnFalseIfExpired() {
        activePremium.setEndDate(Instant.now().minus(1, ChronoUnit.DAYS));
        when(subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("u1")).thenReturn(Optional.of(activePremium));

        assertFalse(subscriptionService.isPremium("u1"));
    }
}
