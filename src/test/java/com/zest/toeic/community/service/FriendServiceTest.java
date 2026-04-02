package com.zest.toeic.community.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.dto.FriendInfo;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.repository.FriendRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .displayName("Alice")
                .level(2)
                .totalXp(600L)
                .streakCurrent(5)
                .build();
        mockUser.setId("user2");
    }

    @Test
    void sendRequest_ToSelf_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> friendService.sendRequest("user1", "user1"));
    }

    @Test
    void sendRequest_ReceiverNotFound_ThrowsNotFound() {
        when(userRepository.findById("user2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> friendService.sendRequest("user1", "user2"));
    }

    @Test
    void sendRequest_AlreadyExists_ThrowsBadRequest() {
        when(userRepository.findById("user2")).thenReturn(Optional.of(mockUser));
        when(friendRepository.findBetweenUsers("user1", "user2"))
                .thenReturn(Optional.of(Friend.builder().status("PENDING").build()));

        assertThrows(BadRequestException.class,
                () -> friendService.sendRequest("user1", "user2"));
    }

    @Test
    void sendRequest_Success() {
        when(userRepository.findById("user2")).thenReturn(Optional.of(mockUser));
        when(friendRepository.findBetweenUsers("user1", "user2")).thenReturn(Optional.empty());
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));

        Friend result = friendService.sendRequest("user1", "user2");

        assertEquals("user1", result.getSenderId());
        assertEquals("user2", result.getReceiverId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void acceptRequest_NotReceiver_ThrowsBadRequest() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user3").status("PENDING").build();
        friend.setId("req1");
        when(friendRepository.findById("req1")).thenReturn(Optional.of(friend));

        assertThrows(BadRequestException.class,
                () -> friendService.acceptRequest("user2", "req1"));
    }

    @Test
    void acceptRequest_NotPending_ThrowsBadRequest() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("ACCEPTED").build();
        friend.setId("req1");
        when(friendRepository.findById("req1")).thenReturn(Optional.of(friend));

        assertThrows(BadRequestException.class,
                () -> friendService.acceptRequest("user2", "req1"));
    }

    @Test
    void acceptRequest_Success() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("PENDING").build();
        friend.setId("req1");
        when(friendRepository.findById("req1")).thenReturn(Optional.of(friend));
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));

        Friend result = friendService.acceptRequest("user2", "req1");

        assertEquals("ACCEPTED", result.getStatus());
    }

    @Test
    void rejectRequest_Success() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("PENDING").build();
        friend.setId("req1");
        when(friendRepository.findById("req1")).thenReturn(Optional.of(friend));

        friendService.rejectRequest("user2", "req1");

        verify(friendRepository).delete(friend);
    }

    @Test
    void unfriend_NotInFriendship_ThrowsBadRequest() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("ACCEPTED").build();
        friend.setId("f1");
        when(friendRepository.findById("f1")).thenReturn(Optional.of(friend));

        assertThrows(BadRequestException.class,
                () -> friendService.unfriend("user3", "f1"));
    }

    @Test
    void unfriend_Success() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("ACCEPTED").build();
        friend.setId("f1");
        when(friendRepository.findById("f1")).thenReturn(Optional.of(friend));

        friendService.unfriend("user1", "f1");

        verify(friendRepository).delete(friend);
    }

    @Test
    void getFriends_ReturnsFriendInfoList() {
        Friend friend = Friend.builder().senderId("user1").receiverId("user2").status("ACCEPTED").build();
        when(friendRepository.findAcceptedFriends("user1")).thenReturn(List.of(friend));
        when(userRepository.findById("user2")).thenReturn(Optional.of(mockUser));

        List<FriendInfo> result = friendService.getFriends("user1");

        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getFriendId());
        assertEquals("Alice", result.get(0).getDisplayName());
        assertEquals(2, result.get(0).getLevel());
    }

    @Test
    void getFriends_DeletedUser_ReturnsPlaceholder() {
        Friend friend = Friend.builder().senderId("user1").receiverId("deleted-user").status("ACCEPTED").build();
        when(friendRepository.findAcceptedFriends("user1")).thenReturn(List.of(friend));
        when(userRepository.findById("deleted-user")).thenReturn(Optional.empty());

        List<FriendInfo> result = friendService.getFriends("user1");

        assertEquals(1, result.size());
        assertEquals("(deleted)", result.get(0).getDisplayName());
    }

    @Test
    void getPendingRequests_Success() {
        Friend friend = Friend.builder().senderId("user2").receiverId("user1").status("PENDING").build();
        when(friendRepository.findByReceiverIdAndStatus("user1", "PENDING")).thenReturn(List.of(friend));
        when(userRepository.findById("user2")).thenReturn(Optional.of(mockUser));

        List<FriendInfo> result = friendService.getPendingRequests("user1");

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }
}
