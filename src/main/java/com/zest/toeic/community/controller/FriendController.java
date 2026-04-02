package com.zest.toeic.community.controller;

import com.zest.toeic.community.dto.FriendInfo;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.service.FriendService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friends")
@Tag(name = "Friends", description = "Friend management — add, accept, reject, unfriend")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/request")
    @Operation(summary = "Gửi lời mời kết bạn")
    public ResponseEntity<ApiResponse<Friend>> sendRequest(
            Authentication auth, @RequestBody Map<String, String> body) {
        String userId = (String) auth.getPrincipal();
        Friend friend = friendService.sendRequest(userId, body.get("targetUserId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(friend));
    }

    @PostMapping("/accept/{id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<ApiResponse<Friend>> acceptRequest(
            Authentication auth, @PathVariable String id) {
        String userId = (String) auth.getPrincipal();
        Friend friend = friendService.acceptRequest(userId, id);
        return ResponseEntity.ok(ApiResponse.success(friend));
    }

    @PostMapping("/reject/{id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            Authentication auth, @PathVariable String id) {
        String userId = (String) auth.getPrincipal();
        friendService.rejectRequest(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy kết bạn")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            Authentication auth, @PathVariable String id) {
        String userId = (String) auth.getPrincipal();
        friendService.unfriend(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    @Operation(summary = "Danh sách bạn bè")
    public ResponseEntity<ApiResponse<List<FriendInfo>>> getFriends(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(friendService.getFriends(userId)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Lời mời kết bạn đang chờ")
    public ResponseEntity<ApiResponse<List<FriendInfo>>> getPendingRequests(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(friendService.getPendingRequests(userId)));
    }
}
