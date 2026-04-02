package com.zest.toeic.gamification.controller;

import com.zest.toeic.gamification.model.DailyQuest;
import com.zest.toeic.gamification.service.QuestService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quests")
@Tag(name = "Daily Quests", description = "Daily quests — view, update progress, claim rewards")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Lấy daily quests hôm nay (tự generate nếu chưa có)")
    public ResponseEntity<ApiResponse<DailyQuest>> getDailyQuests(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        DailyQuest dq = questService.getOrGenerateQuests(userId);
        return ResponseEntity.ok(ApiResponse.success(dq));
    }

    @PostMapping("/daily/progress")
    @Operation(summary = "Cập nhật tiến độ quest (gọi khi user hoàn thành action)")
    public ResponseEntity<ApiResponse<DailyQuest>> updateProgress(
            Authentication auth, @RequestBody Map<String, Object> body) {
        String userId = (String) auth.getPrincipal();
        String questType = (String) body.get("questType");
        int amount = body.get("amount") instanceof Integer i ? i : Integer.parseInt(body.get("amount").toString());
        DailyQuest dq = questService.updateProgress(userId, questType, amount);
        return ResponseEntity.ok(ApiResponse.success(dq));
    }

    @PostMapping("/daily/claim/{index}")
    @Operation(summary = "Nhận phần thưởng quest đã hoàn thành")
    public ResponseEntity<ApiResponse<DailyQuest>> claimReward(
            Authentication auth, @PathVariable int index) {
        String userId = (String) auth.getPrincipal();
        DailyQuest dq = questService.claimReward(userId, index);
        return ResponseEntity.ok(ApiResponse.success(dq));
    }
}
