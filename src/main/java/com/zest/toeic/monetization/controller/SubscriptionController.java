package com.zest.toeic.monetization.controller;

import com.zest.toeic.monetization.model.Subscription;
import com.zest.toeic.monetization.service.SubscriptionService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Premium subscription & VNPay payment")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/me")
    @Operation(summary = "Xem subscription hiện tại")
    public ResponseEntity<ApiResponse<Subscription>> getCurrentPlan(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getCurrentSubscription(auth.getName())));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Tạo URL thanh toán VNPay để upgrade Premium")
    public ResponseEntity<ApiResponse<Map<String, String>>> upgradeToPremium(
            Authentication auth, HttpServletRequest request) {
        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isEmpty()) {
            ipAddr = request.getRemoteAddr();
        }
        String paymentUrl = subscriptionService.initiatePremiumUpgrade(auth.getName(), ipAddr);
        return ResponseEntity.ok(ApiResponse.success(Map.of("paymentUrl", paymentUrl)));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Hủy subscription Premium")
    public ResponseEntity<ApiResponse<Subscription>> cancelSubscription(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.cancelSubscription(auth.getName())));
    }

    @GetMapping("/check-premium")
    @Operation(summary = "Kiểm tra user có Premium không")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkPremium(Authentication auth) {
        boolean isPremium = subscriptionService.isPremium(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(Map.of("isPremium", isPremium)));
    }
}
