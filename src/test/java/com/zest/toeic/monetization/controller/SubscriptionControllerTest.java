package com.zest.toeic.monetization.controller;

import com.zest.toeic.monetization.model.Subscription;
import com.zest.toeic.monetization.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("u1", "auth");
    }

    @Test
    void upgradeToPremium_returnsOk() throws Exception {
        when(subscriptionService.initiatePremiumUpgrade(anyString(), anyString())).thenReturn("http://vnpay");

        mockMvc.perform(post("/api/v1/subscriptions/upgrade")
                .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentPlan_returnsOk() throws Exception {
        Subscription sub = Subscription.builder().plan("PREMIUM").build();
        when(subscriptionService.getCurrentSubscription(anyString())).thenReturn(sub);

        mockMvc.perform(get("/api/v1/subscriptions/me")
                .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void checkPremium_returnsOk() throws Exception {
        when(subscriptionService.isPremium(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/subscriptions/check-premium")
                .principal(principal))
                .andExpect(status().isOk());
    }
}
