package com.zest.toeic.admin.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("test@example.com")
                .displayName("John Doe")
                .status(UserStatus.ACTIVE)
                .build();
        mockUser.setId("u1");
    }

    @Test
    void listUsers_withSearchFilter() {
        Page<User> page = new PageImpl<>(List.of(mockUser));
        when(userRepository.findByDisplayNameContainingIgnoreCase(eq("John"), any(PageRequest.class))).thenReturn(page);

        Page<User> result = adminUserService.listUsers("John", null, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listUsers_withStatusFilter() {
        Page<User> page = new PageImpl<>(List.of(mockUser));
        when(userRepository.findByStatus(eq(UserStatus.ACTIVE), any(PageRequest.class))).thenReturn(page);

        Page<User> result = adminUserService.listUsers(null, "ACTIVE", 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listUsers_noFilters() {
        Page<User> page = new PageImpl<>(List.of(mockUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<User> result = adminUserService.listUsers(null, null, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUser_shouldReturnUser() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        User result = adminUserService.getUser("u1");
        assertEquals("John Doe", result.getDisplayName());
    }

    @Test
    void getUser_shouldThrowNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUser("u1"));
    }

    @Test
    void suspendUser_shouldSetStatusSuspended() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminUserService.suspendUser("u1", "Violation");
        assertEquals(UserStatus.SUSPENDED, result.getStatus());
    }

    @Test
    void banUser_shouldSetStatusBanned() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminUserService.banUser("u1");
        assertEquals(UserStatus.BANNED, result.getStatus());
    }

    @Test
    void activateUser_shouldSetStatusActive() {
        mockUser.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminUserService.activateUser("u1");
        assertEquals(UserStatus.ACTIVE, result.getStatus());
    }
}
