package com.zest.toeic.community.controller;

import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.service.SquadService;
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
@RequestMapping("/api/v1/squads")
@Tag(name = "Squads", description = "Squad management — create, join, leave, kick")
public class SquadController {

    private final SquadService squadService;

    public SquadController(SquadService squadService) {
        this.squadService = squadService;
    }

    @PostMapping
    @Operation(summary = "Tạo squad mới (yêu cầu Level ≥ 2)")
    public ResponseEntity<ApiResponse<Squad>> createSquad(
            Authentication auth, @RequestBody Map<String, String> body) {
        String userId = (String) auth.getPrincipal();
        Squad squad = squadService.createSquad(userId, body.get("name"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(squad));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Tham gia squad")
    public ResponseEntity<ApiResponse<Squad>> joinSquad(
            Authentication auth, @PathVariable String id) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(squadService.joinSquad(userId, id)));
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Rời squad")
    public ResponseEntity<ApiResponse<Squad>> leaveSquad(
            Authentication auth, @PathVariable String id) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(squadService.leaveSquad(userId, id)));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Kick thành viên (owner only)")
    public ResponseEntity<ApiResponse<Squad>> kickMember(
            Authentication auth, @PathVariable String id, @PathVariable String userId) {
        String ownerId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(squadService.kickMember(ownerId, id, userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết squad")
    public ResponseEntity<ApiResponse<Squad>> getSquadDetails(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(squadService.getSquadDetails(id)));
    }

    @GetMapping("/my")
    @Operation(summary = "Danh sách squads của tôi")
    public ResponseEntity<ApiResponse<List<Squad>>> getMySquads(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(squadService.getMySquads(userId)));
    }
}
