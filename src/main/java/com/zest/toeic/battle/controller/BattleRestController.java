package com.zest.toeic.battle.controller;
import com.zest.toeic.battle.model.BattleParticipant;
import com.zest.toeic.battle.model.Battle;
import com.zest.toeic.battle.service.BattleService;

import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Battle Royale", description = "PvP Battle Royale management")
public class BattleRestController {

    private final BattleService battleService;
    private final BattleWebSocketController wsController;

    public BattleRestController(BattleService battleService, BattleWebSocketController wsController) {
        this.battleService = battleService;
        this.wsController = wsController;
    }

    @GetMapping("/api/v1/battles")
    @Operation(summary = "List upcoming and recent battles")
    public ResponseEntity<ApiResponse<List<Battle>>> listBattles() {
        return ResponseEntity.ok(ApiResponse.success(battleService.getActiveBattles()));
    }

    @GetMapping("/api/v1/battles/{id}")
    @Operation(summary = "Get battle details")
    public ResponseEntity<ApiResponse<Battle>> getBattle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(battleService.getBattle(id)));
    }

    @PostMapping("/api/v1/battles/{id}/register")
    @Operation(summary = "Register for a battle")
    public ResponseEntity<ApiResponse<BattleParticipant>> register(
            @PathVariable String id, Authentication auth) {
        BattleParticipant p = battleService.register(id, auth.getName(), auth.getName());
        // Real-time broadcast: new player joined
        wsController.broadcastPlayerJoined(id, p);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(p));
    }

    @GetMapping("/api/v1/battles/{id}/results")
    @Operation(summary = "Get battle results")
    public ResponseEntity<ApiResponse<List<BattleParticipant>>> getResults(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(battleService.getResults(id)));
    }

    // ═══════ Admin ═══════

    @PostMapping("/api/v1/admin/battles")
    @Operation(summary = "Schedule a battle (Admin)")
    public ResponseEntity<ApiResponse<Battle>> scheduleBattle(@RequestBody Battle battle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(battleService.scheduleBattle(battle)));
    }

    @PostMapping("/api/v1/admin/battles/{id}/start")
    @Operation(summary = "Start a battle (Admin)")
    public ResponseEntity<ApiResponse<Battle>> startBattle(@PathVariable String id) {
        Battle battle = battleService.startBattle(id);
        // Real-time broadcast: battle started
        wsController.broadcastStart(id);
        return ResponseEntity.ok(ApiResponse.success(battle));
    }

    @PostMapping("/api/v1/admin/battles/{id}/end")
    @Operation(summary = "End a battle (Admin)")
    public ResponseEntity<ApiResponse<Battle>> endBattle(@PathVariable String id) {
        Battle battle = battleService.endBattle(id);
        // Real-time broadcast: battle ended + final results
        wsController.broadcastEnd(id);
        return ResponseEntity.ok(ApiResponse.success(battle));
    }
}
