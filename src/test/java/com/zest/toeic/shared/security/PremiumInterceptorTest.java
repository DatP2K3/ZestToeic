package com.zest.toeic.shared.security;

import com.zest.toeic.monetization.service.SubscriptionService;
import com.zest.toeic.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PremiumInterceptorTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HandlerMethod handlerMethod;

    @InjectMocks private PremiumInterceptor premiumInterceptor;

    @Test
    void preHandle_NotHandlerMethod_ReturnsTrue() {
        assertTrue(premiumInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_NoAnnotation_ReturnsTrue() {
        when(handlerMethod.getMethodAnnotation(PremiumRequired.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);

        assertTrue(premiumInterceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    void preHandle_WithAnnotation_ThrowsUnAuth_IfNoSecurityContext() {
        PremiumRequired mockAnnotation = mock(PremiumRequired.class);
        when(handlerMethod.getMethodAnnotation(PremiumRequired.class)).thenReturn(mockAnnotation);
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, () -> premiumInterceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    void preHandle_WithAnnotation_ThrowsUnAuth_IfNotPremium() {
        PremiumRequired mockAnnotation = mock(PremiumRequired.class);
        when(handlerMethod.getMethodAnnotation(PremiumRequired.class)).thenReturn(mockAnnotation);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "pass", java.util.List.of())
        );
        when(subscriptionService.isPremium("user1")).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> premiumInterceptor.preHandle(request, response, handlerMethod));
        assertTrue(ex.getMessage().contains("Premium"));
    }

    @Test
    void preHandle_WithAnnotation_ReturnsTrue_IfPremium() {
        PremiumRequired mockAnnotation = mock(PremiumRequired.class);
        when(handlerMethod.getMethodAnnotation(PremiumRequired.class)).thenReturn(mockAnnotation);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "pass", java.util.List.of())
        );
        when(subscriptionService.isPremium("user1")).thenReturn(true);

        assertTrue(premiumInterceptor.preHandle(request, response, handlerMethod));
    }
}
