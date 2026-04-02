package com.zest.toeic.shared.security;

import com.zest.toeic.monetization.service.SubscriptionService;
import com.zest.toeic.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PremiumInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;

    public PremiumInterceptor(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        PremiumRequired annotation = handlerMethod.getMethodAnnotation(PremiumRequired.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(PremiumRequired.class);
        }
        if (annotation == null) {
            return true; // No premium check needed
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        String userId = auth.getName();
        if (!subscriptionService.isPremium(userId)) {
            throw new UnauthorizedException("Tính năng này yêu cầu gói Premium. Vui lòng nâng cấp tài khoản.");
        }

        return true;
    }
}
