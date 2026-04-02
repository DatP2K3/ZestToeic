package com.zest.toeic.gamification.controller;

import com.zest.toeic.gamification.dto.XpSummary;
import com.zest.toeic.gamification.model.UserStreak;
import com.zest.toeic.gamification.model.XpTransaction;
import com.zest.toeic.gamification.service.GamificationService;
import com.zest.toeic.gamification.service.StreakService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
@Tag(name = "Gamification", description = "XP, Levels, Streaks, and Rewards")
public class GamificationController {

    private final GamificationService gamificationService;
    private final StreakService streakService;

    public GamificationController(GamificationService gamificationService, StreakService streakService) {
        this.gamificationService = gamificationService;
        this.streakService = streakService;
    }

    @GetMapping("/xp")
    @Operation(summary = "XP summary — level, progress, daily cap")
    public ResponseEntity<ApiResponse<XpSummary>> getXpSummary(Authentication auth) {
        XpSummary summary = gamificationService.getXpSummary(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/xp/history")
    @Operation(summary = "Lịch sử XP transactions")
    public ResponseEntity<ApiResponse<Page<XpTransaction>>> getXpHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Page<XpTransaction> history = gamificationService.getXpHistory(auth.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/streak")
    @Operation(summary = "Thông tin streak hiện tại")
    public ResponseEntity<ApiResponse<UserStreak>> getStreak(Authentication auth) {
        UserStreak streak = streakService.getStreakInfo(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(streak));
    }

    @PostMapping("/streak/record")
    @Operation(summary = "Ghi nhận hoạt động hôm nay cho streak")
    public ResponseEntity<ApiResponse<UserStreak>> recordActivity(Authentication auth) {
        UserStreak streak = streakService.recordActivity(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(streak));
    }
}
