package com.zest.toeic.gamification.controller;

import com.zest.toeic.gamification.dto.LeaderboardEntry;
import com.zest.toeic.gamification.service.LeaderboardService;
import com.zest.toeic.shared.dto.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ApiResponse<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "WEEKLY") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(leaderboardService.getLeaderboard(period, page, size));
    }

    @GetMapping("/me")
    public ApiResponse<LeaderboardEntry> getMyRank(
            Authentication auth,
            @RequestParam(defaultValue = "WEEKLY") String period) {
        return ApiResponse.success(leaderboardService.getUserRank(auth.getName(), period));
    }

    @GetMapping("/friends")
    public ApiResponse<List<LeaderboardEntry>> getFriendsLeaderboard(
            Authentication auth,
            @RequestParam(defaultValue = "WEEKLY") String period) {
        return ApiResponse.success(leaderboardService.getFriendsLeaderboard(auth.getName(), period));
    }
}
