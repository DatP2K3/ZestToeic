package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.dto.StartTestRequest;
import com.zest.toeic.practice.dto.TestAnswerRequest;
import com.zest.toeic.practice.dto.TestAnswerResponse;
import com.zest.toeic.practice.dto.TestResult;
import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.practice.service.TestService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tests")
@Tag(name = "Tests", description = "Placement, Mock, and Mini tests")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/placement/start")
    @Operation(summary = "Bắt đầu Placement Test — 25 câu adaptive")
    public ResponseEntity<ApiResponse<TestSession>> startPlacementTest(Authentication auth) {
        TestSession session = testService.startPlacementTest(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(session));
    }

    @PostMapping("/mock/start")
    @Operation(summary = "Bắt đầu Full Mock Test — 200 câu, 120 phút")
    public ResponseEntity<ApiResponse<TestSession>> startMockTest(Authentication auth) {
        TestSession session = testService.startMockTest(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(session));
    }

    @PostMapping("/mini/start")
    @Operation(summary = "Bắt đầu Mini Test — tùy chọn Part, difficulty, số câu")
    public ResponseEntity<ApiResponse<TestSession>> startMiniTest(
            @Valid @RequestBody StartTestRequest request,
            Authentication auth) {
        TestSession session = testService.startMiniTest(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(session));
    }

    @PostMapping("/{testId}/answer")
    @Operation(summary = "Submit câu trả lời trong test")
    public ResponseEntity<ApiResponse<TestAnswerResponse>> submitAnswer(
            Authentication auth,
            @PathVariable String testId,
            @Valid @RequestBody TestAnswerRequest request) {
        TestAnswerResponse result = testService.submitTestAnswer(auth.getName(), testId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{testId}/complete")
    @Operation(summary = "Kết thúc test và nhận kết quả")
    public ResponseEntity<ApiResponse<TestResult>> completeTest(
            @PathVariable String testId,
            Authentication auth) {
        TestResult result = testService.completeTest(auth.getName(), testId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{testId}")
    @Operation(summary = "Xem chi tiết test session")
    public ResponseEntity<ApiResponse<TestSession>> getTest(
            @PathVariable String testId,
            Authentication auth) {
        TestSession session = testService.getTestSession(auth.getName(), testId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/history")
    @Operation(summary = "Lịch sử test theo loại")
    public ResponseEntity<ApiResponse<Page<TestSession>>> getTestHistory(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Page<TestSession> history = testService.getTestHistory(auth.getName(), type, page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
