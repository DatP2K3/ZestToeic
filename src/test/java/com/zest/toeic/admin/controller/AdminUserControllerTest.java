package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.service.AdminUserService;
import com.zest.toeic.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchUsers_returnsPage() throws Exception {
        when(adminUserService.listUsers(any(), any(), anyInt(), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void suspendUser_returnsOk() throws Exception {
        when(adminUserService.suspendUser(anyString(), anyString())).thenReturn(User.builder().build());
        mockMvc.perform(put("/api/v1/admin/users/1/suspend").param("reason", "spam"))
                .andExpect(status().isOk());
    }

    @Test
    void activateUser_returnsOk() throws Exception {
        when(adminUserService.activateUser(anyString())).thenReturn(User.builder().build());
        mockMvc.perform(put("/api/v1/admin/users/1/activate"))
                .andExpect(status().isOk());
    }
}
