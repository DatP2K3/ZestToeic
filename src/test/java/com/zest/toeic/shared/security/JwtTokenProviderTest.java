package com.zest.toeic.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                900000,  // 15 min
                604800000 // 7 days
        );
    }

    @Test
    void generateAccessToken_returnsValidToken() {
        String token = jwtTokenProvider.generateAccessToken("user123", "test@email.com", "USER");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("access", jwtTokenProvider.getTokenType(token));
        assertEquals("USER", jwtTokenProvider.getRole(token));
    }

    @Test
    void generateRefreshToken_returnsValidToken() {
        String token = jwtTokenProvider.generateRefreshToken("user123");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("refresh", jwtTokenProvider.getTokenType(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void accessAndRefreshTokens_haveDifferentTypes() {
        String access = jwtTokenProvider.generateAccessToken("user1", "a@b.com", "USER");
        String refresh = jwtTokenProvider.generateRefreshToken("user1");

        assertEquals("access", jwtTokenProvider.getTokenType(access));
        assertEquals("refresh", jwtTokenProvider.getTokenType(refresh));
        assertNotEquals(access, refresh);
    }
}
