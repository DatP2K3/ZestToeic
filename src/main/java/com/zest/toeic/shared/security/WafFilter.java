package com.zest.toeic.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight Application-Level WAF (Web Application Firewall)
 * Filters IP for payment webhooks to prevent external DDOS & Spoofing.
 */
@Component
public class WafFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WafFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    // VNPay Sandbox & Prod IPs (example: 113.160.92.202, 113.52.45.78, etc.)
    // Localhost is allowed for development "0:0:0:0:0:0:0:1", "127.0.0.1"
    private static final Set<String> ALLOWED_IPN_IPS = Set.of(
            "113.160.92.202", "113.52.45.78", "116.118.49.43", "116.118.49.44",
            "127.0.0.1", "0:0:0:0:0:0:0:1"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. WAF Caching / Basic Rate Limiting
        String requestURI = request.getRequestURI();
        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();
        
        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));
        lastRequestTime.putIfAbsent(clientIp, currentTime);
        
        if (currentTime - lastRequestTime.get(clientIp) > 60000) { // Reset every minute
            requestCounts.get(clientIp).set(0);
            lastRequestTime.put(clientIp, currentTime);
        }
        
        int count = requestCounts.get(clientIp).incrementAndGet();
        if (count > MAX_REQUESTS_PER_MINUTE) {
            log.warn("[WAF RATE LIMIT] IP {} blocked for exceeding limit", clientIp);
            response.sendError(429, "Rate limit exceeded. Please try again later.");
            return;
        }

        // 2. IP Whitelisting for VNPay IPN (Webhook)
        if (requestURI.contains("/payment/vnpay-ipn")) {
            if (!ALLOWED_IPN_IPS.contains(clientIp)) {
                log.warn("[WAF BLOCK] Unauthorized IP {} attempted to access VNPAY IPN at {}", clientIp, requestURI);
                // For MVP, logging only. In Prod, uncomment below:
                // response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied by WAF");
                // return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
