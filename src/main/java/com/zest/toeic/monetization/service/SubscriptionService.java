package com.zest.toeic.monetization.service;

import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.monetization.event.SubscriptionUpdatedEvent;
import com.zest.toeic.monetization.model.Subscription;
import com.zest.toeic.monetization.repository.SubscriptionRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class SubscriptionService {

    private static final long PREMIUM_PRICE_VND = 99000;

    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VNPayService vnPayService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               ApplicationEventPublisher eventPublisher,
                               VNPayService vnPayService) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.vnPayService = vnPayService;
    }

    public Subscription getCurrentSubscription(String userId) {
        return subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseGet(() -> Subscription.builder().userId(userId).plan("FREE").status("ACTIVE").build());
    }

    public String initiatePremiumUpgrade(String userId, String ipAddr) {
        Subscription current = getCurrentSubscription(userId);
        if ("PREMIUM".equals(current.getPlan()) && "ACTIVE".equals(current.getStatus())) {
            throw new BadRequestException("Already a Premium subscriber");
        }

        String orderInfo = "ZestTOEIC Premium 1 thang - User " + userId;
        return vnPayService.createPaymentUrl(userId, PREMIUM_PRICE_VND, orderInfo, ipAddr);
    }

    public Subscription activatePremium(String userId, String transactionId) {
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .plan("PREMIUM")
                .status("ACTIVE")
                .startDate(Instant.now())
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .autoRenew(true)
                .paymentMethod("VNPAY")
                .lastTransactionId(transactionId)
                .build();

        eventPublisher.publishEvent(new SubscriptionUpdatedEvent(userId, "PREMIUM"));

        return subscriptionRepository.save(subscription);
    }

    public Subscription cancelSubscription(String userId) {
        Subscription sub = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new BadRequestException("No active subscription found"));

        sub.setAutoRenew(false);
        sub.setStatus("CANCELLED");

        eventPublisher.publishEvent(new SubscriptionUpdatedEvent(userId, "FREE"));

        return subscriptionRepository.save(sub);
    }

    public boolean isPremium(String userId) {
        Subscription sub = getCurrentSubscription(userId);
        return "PREMIUM".equals(sub.getPlan()) && "ACTIVE".equals(sub.getStatus())
                && (sub.getEndDate() == null || sub.getEndDate().isAfter(Instant.now()));
    }
}
