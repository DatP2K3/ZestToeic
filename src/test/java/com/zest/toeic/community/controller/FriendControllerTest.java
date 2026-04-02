package com.zest.toeic.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.community.dto.FriendInfo;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.service.FriendService;
import com.zest.toeic.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FriendControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FriendService friendService;

    @InjectMocks
    private FriendController friendController;

    private Authentication principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(friendController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        principal = new UsernamePasswordAuthenticationToken("user1", "auth");
    }

    @Test
    void sendRequest_Success() throws Exception {
        Friend friend = Friend.builder()
                .senderId("user1").receiverId("user2").status("PENDING").build();
        when(friendService.sendRequest("user1", "user2")).thenReturn(friend);

        mockMvc.perform(post("/api/v1/friends/request")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetUserId", "user2"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.senderId").value("user1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void acceptRequest_Success() throws Exception {
        Friend friend = Friend.builder()
                .senderId("user2").receiverId("user1").status("ACCEPTED").build();
        when(friendService.acceptRequest("user1", "req1")).thenReturn(friend);

        mockMvc.perform(post("/api/v1/friends/accept/req1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void rejectRequest_Success() throws Exception {
        doNothing().when(friendService).rejectRequest("user1", "req1");

        mockMvc.perform(post("/api/v1/friends/reject/req1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(friendService).rejectRequest("user1", "req1");
    }

    @Test
    void unfriend_Success() throws Exception {
        doNothing().when(friendService).unfriend("user1", "f1");

        mockMvc.perform(delete("/api/v1/friends/f1")
                        .principal(principal))
                .andExpect(status().isOk());

        verify(friendService).unfriend("user1", "f1");
    }

    @Test
    void getFriends_Success() throws Exception {
        List<FriendInfo> friends = List.of(
                FriendInfo.builder().friendId("user2").displayName("Alice").level(3).status("ACCEPTED").build()
        );
        when(friendService.getFriends("user1")).thenReturn(friends);

        mockMvc.perform(get("/api/v1/friends")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].friendId").value("user2"))
                .andExpect(jsonPath("$.data[0].displayName").value("Alice"));
    }

    @Test
    void getPendingRequests_Success() throws Exception {
        List<FriendInfo> pending = List.of(
                FriendInfo.builder().friendId("user3").displayName("Bob").status("PENDING").build()
        );
        when(friendService.getPendingRequests("user1")).thenReturn(pending);

        mockMvc.perform(get("/api/v1/friends/pending")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
