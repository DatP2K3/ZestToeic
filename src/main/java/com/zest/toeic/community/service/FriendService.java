package com.zest.toeic.community.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.dto.FriendInfo;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.repository.FriendRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.FriendStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FriendService {

    private static final Logger log = LoggerFactory.getLogger(FriendService.class);

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public Friend sendRequest(String senderId, String receiverId) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Không thể gửi lời mời kết bạn cho chính mình");
        }

        userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại: " + receiverId));

        friendRepository.findBetweenUsers(senderId, receiverId)
                .ifPresent(existing -> {
                    throw new BadRequestException("Đã tồn tại quan hệ bạn bè (status: " + existing.getStatus() + ")");
                });

        Friend friend = Friend.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendStatus.PENDING)
                .build();

        Friend saved = friendRepository.save(friend);
        log.info("Friend request sent: {} → {}", senderId, receiverId);
        return saved;
    }

    public Friend acceptRequest(String receiverId, String requestId) {
        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request không tồn tại"));

        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BadRequestException("Bạn không có quyền chấp nhận lời mời này");
        }

        if (FriendStatus.PENDING != friend.getStatus()) {
            throw new BadRequestException("Lời mời đã được xử lý (status: " + friend.getStatus() + ")");
        }

        friend.setStatus(FriendStatus.ACCEPTED);
        Friend saved = friendRepository.save(friend);
        log.info("Friend request accepted: {} ← {}", friend.getSenderId(), receiverId);
        return saved;
    }

    public void rejectRequest(String receiverId, String requestId) {
        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request không tồn tại"));

        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BadRequestException("Bạn không có quyền từ chối lời mời này");
        }

        friendRepository.delete(friend);
        log.info("Friend request rejected: {} ← {}", friend.getSenderId(), receiverId);
    }

    public void unfriend(String userId, String friendshipId) {
        Friend friend = friendRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Quan hệ bạn bè không tồn tại"));

        if (!friend.getSenderId().equals(userId) && !friend.getReceiverId().equals(userId)) {
            throw new BadRequestException("Bạn không thuộc quan hệ bạn bè này");
        }

        friendRepository.delete(friend);
        log.info("Unfriended: {} removed friendship {}", userId, friendshipId);
    }

    public List<FriendInfo> getFriends(String userId) {
        List<Friend> friends = friendRepository.findAcceptedFriends(userId);
        return friends.stream()
                .map(f -> {
                    String friendId = f.getSenderId().equals(userId) ? f.getReceiverId() : f.getSenderId();
                    return buildFriendInfo(friendId, f.getStatus());
                })
                .toList();
    }

    public List<FriendInfo> getPendingRequests(String userId) {
        List<Friend> pending = friendRepository.findByReceiverIdAndStatus(userId, FriendStatus.PENDING);
        return pending.stream()
                .map(f -> buildFriendInfo(f.getSenderId(), FriendStatus.PENDING))
                .toList();
    }

    private FriendInfo buildFriendInfo(String userId, FriendStatus status) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return FriendInfo.builder().friendId(userId).status(status).displayName("(deleted)").build();
        }
        return FriendInfo.builder()
                .friendId(user.getId())
                .displayName(user.getDisplayName())
                .level(user.getLevel())
                .totalXp(user.getTotalXp())
                .streakCurrent(user.getStreakCurrent())
                .status(status)
                .build();
    }
}
