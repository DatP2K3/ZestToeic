package com.zest.toeic.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private final long accessExp = 900000; // 15 min
    private final long refreshExp = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, accessExp, refreshExp);
    }

    @Test
    void tokenLifecycle_AccessToken_Success() {
        String token = jwtTokenProvider.generateAccessToken("user123", "test@email.com", "USER");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("access", jwtTokenProvider.getTokenType(token));
        assertEquals("USER", jwtTokenProvider.getRole(token));
    }

    @Test
    void tokenLifecycle_RefreshToken_Success() {
        String token = jwtTokenProvider.generateRefreshToken("user123");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("refresh", jwtTokenProvider.getTokenType(token));
        assertNull(jwtTokenProvider.getRole(token)); // Refresh token doesn't have role
    }

    @Test
    void validateToken_InvalidSignature_ReturnsFalse() {
        String token = jwtTokenProvider.generateAccessToken("u1", "t@t.com", "USER");
        String invalidToken = token + "bad";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_NullOrEmptyToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void accessAndRefreshTokens_haveDifferentTypes() {
        String access = jwtTokenProvider.generateAccessToken("user1", "a@b.com", "USER");
        String refresh = jwtTokenProvider.generateRefreshToken("user1");

        assertEquals("access", jwtTokenProvider.getTokenType(access));
        assertEquals("refresh", jwtTokenProvider.getTokenType(refresh));
        assertNotEquals(access, refresh);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        // Create an provider with a 1 ms expiration specifically for testing expiration logic
        JwtTokenProvider fastExpiringProvider = new JwtTokenProvider(testSecret, 1, 1);
        String token = fastExpiringProvider.generateAccessToken("u1", "t@t.com", "USER");

        // Wait to ensure expiration happens 
        Thread.sleep(10);

        assertFalse(fastExpiringProvider.validateToken(token));
    }
}
