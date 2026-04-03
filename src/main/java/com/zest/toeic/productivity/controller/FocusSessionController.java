package com.zest.toeic.productivity.controller;
import com.zest.toeic.productivity.model.FocusSession;
import com.zest.toeic.productivity.service.FocusSessionService;

import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/focus")
@Tag(name = "Focus Mode", description = "Pomodoro focus timer and statistics")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    public FocusSessionController(FocusSessionService focusSessionService) {
        this.focusSessionService = focusSessionService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start a focus session")
    public ResponseEntity<ApiResponse<FocusSession>> start(
            @RequestParam(defaultValue = "50") int duration,
            @RequestParam(required = false) String task,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(focusSessionService.startSession(auth.getName(), duration, task)));
    }

    @PostMapping("/stop")
    @Operation(summary = "End current focus session")
    public ResponseEntity<ApiResponse<FocusSession>> stop(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(focusSessionService.endSession(auth.getName())));
    }

    @GetMapping("/current")
    @Operation(summary = "Get active focus session")
    public ResponseEntity<ApiResponse<FocusSession>> current(Authentication auth) {
        return focusSessionService.getActiveSession(auth.getName())
                .map(s -> ResponseEntity.ok(ApiResponse.success(s)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get focus statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(focusSessionService.getStatistics(auth.getName())));
    }
}
