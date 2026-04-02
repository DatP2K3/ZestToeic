package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.service.AdminQuestionService;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Questions", description = "Question Bank Management")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping
    @Operation(summary = "Danh sách câu hỏi (phân trang, filter)")
    public ResponseEntity<ApiResponse<Page<Question>>> listQuestions(
            @RequestParam(required = false) Integer part,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminQuestionService.listQuestions(part, status, page, size)));
    }

    @PostMapping
    @Operation(summary = "Tạo câu hỏi mới")
    public ResponseEntity<ApiResponse<Question>> createQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(ApiResponse.success(adminQuestionService.createQuestion(question)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật câu hỏi")
    public ResponseEntity<ApiResponse<Question>> updateQuestion(@PathVariable String id, @RequestBody Question updates) {
        return ResponseEntity.ok(ApiResponse.success(adminQuestionService.updateQuestion(id, updates)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa câu hỏi")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String id) {
        adminQuestionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Duyệt câu hỏi")
    public ResponseEntity<ApiResponse<Question>> approveQuestion(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminQuestionService.approveQuestion(id)));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối câu hỏi")
    public ResponseEntity<ApiResponse<Question>> rejectQuestion(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminQuestionService.rejectQuestion(id)));
    }
}
