package com.zest.toeic.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Application-Level WAF (Web Application Firewall).
 * - Rate limiting via Redis (distributed, auto-eviction).
 * - IP whitelisting for VNPay IPN callbacks.
 */
@Component
public class WafFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WafFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    // VNPay Sandbox & Prod IPs
    private static final Set<String> ALLOWED_IPN_IPS = Set.of(
            "113.160.92.202", "113.52.45.78", "116.118.49.43", "116.118.49.44",
            "127.0.0.1", "0:0:0:0:0:0:0:1"
    );

    @Value("${waf.trusted-proxies:}")
    private List<String> trustedProxies;

    public WafFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String clientIp = getClientIp(request);

        // 1. Rate Limiting via Redis
        if (isRateLimited(clientIp)) {
            log.warn("[WAF RATE LIMIT] IP {} blocked — exceeded {} req/min", clientIp, MAX_REQUESTS_PER_MINUTE);
            response.sendError(429, "Rate limit exceeded. Please try again later.");
            return;
        }

        // 2. IP Whitelisting for VNPay IPN (Webhook) — ENFORCED
        if (requestURI.contains("/payment/vnpay-ipn")) {
            if (!ALLOWED_IPN_IPS.contains(clientIp)) {
                log.warn("[WAF BLOCK] Unauthorized IP {} attempted VNPay IPN at {}", clientIp, requestURI);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied by WAF");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIp) {
        try {
            String key = RATE_LIMIT_PREFIX + clientIp;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, RATE_LIMIT_WINDOW);
            }
            return count != null && count > MAX_REQUESTS_PER_MINUTE;
        } catch (Exception e) {
            // Redis down → fail-open (allow request, log warning)
            log.warn("[WAF] Redis unavailable for rate limiting: {}", e.getMessage());
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        // Only trust X-Forwarded-For from configured trusted proxies
        if (xfHeader != null && !xfHeader.isEmpty() && !xfHeader.equalsIgnoreCase("unknown")) {
            String directIp = request.getRemoteAddr();
            if (trustedProxies != null && !trustedProxies.isEmpty() && trustedProxies.contains(directIp)) {
                return xfHeader.split(",")[0].trim();
            }
            // Untrusted proxy → use direct connection IP
            return directIp;
        }

        return request.getRemoteAddr();
    }
}
