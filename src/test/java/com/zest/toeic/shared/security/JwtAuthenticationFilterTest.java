package com.zest.toeic.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtTokenProvider.validateToken("valid.token.here")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid.token.here")).thenReturn("access");
        when(jwtTokenProvider.getUserIdFromToken("valid.token.here")).thenReturn("user123");
        when(jwtTokenProvider.getRole("valid.token.here")).thenReturn("ADMIN");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user123", auth.getPrincipal());
        assertEquals("[ROLE_ADMIN]", auth.getAuthorities().toString());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoRoleInToken_DefaultsToUSER() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtTokenProvider.validateToken("valid.token.here")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid.token.here")).thenReturn("access");
        when(jwtTokenProvider.getUserIdFromToken("valid.token.here")).thenReturn("user123");
        when(jwtTokenProvider.getRole("valid.token.here")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("[ROLE_USER]", auth.getAuthorities().toString());
    }

    @Test
    void doFilterInternal_NoToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_RefreshTokenProvider_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer refresh.token.here");
        when(jwtTokenProvider.validateToken("refresh.token.here")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("refresh.token.here")).thenReturn("refresh");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
