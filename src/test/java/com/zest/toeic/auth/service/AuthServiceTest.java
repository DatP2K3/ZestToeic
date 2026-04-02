package com.zest.toeic.auth.service;

import com.zest.toeic.auth.dto.AuthResponse;
import com.zest.toeic.auth.dto.LoginRequest;
import com.zest.toeic.auth.dto.RegisterRequest;
import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.shared.exception.DuplicateResourceException;
import com.zest.toeic.shared.exception.UnauthorizedException;
import com.zest.toeic.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashed-password")
                .displayName("Test User")
                .status("ACTIVE")
                .level(1)
                .totalXp(0L)
                .role("USER")
                .subscriptionTier("FREE")
                .build();
        mockUser.setId("u1");
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass123");
        req.setDisplayName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse res = authService.register(req);

        assertEquals("access-token", res.getAccessToken());
        assertEquals("refresh-token", res.getRefreshToken());
        assertEquals("test@example.com", res.getUser().getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("TEST@example.com");
        req.setPassword("pass123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("pass123", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse res = authService.login(req);

        assertEquals("access-token", res.getAccessToken());
        assertEquals("u1", res.getUser().getId());
    }

    @Test
    void login_WrongEmail_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("wrong@example.com");

        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void login_BannedUser_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass123");

        mockUser.setStatus("BANNED");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("pass123", "hashed-password")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void refreshToken_Success() {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid-token")).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));

        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh");

        AuthResponse res = authService.refreshToken("valid-token");

        assertEquals("new-access", res.getAccessToken());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("invalid"));
    }

    @Test
    void refreshToken_WrongTokenType_ThrowsException() {
        when(jwtTokenProvider.validateToken("access-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("access-token")).thenReturn("access");

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("access-token"));
    }

    @Test
    void refreshToken_UserNotFound_ThrowsException() {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid-token")).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("valid-token"));
    }
}
