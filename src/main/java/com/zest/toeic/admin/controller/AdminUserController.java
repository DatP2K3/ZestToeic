package com.zest.toeic.admin.controller;

import com.zest.toeic.admin.service.AdminUserService;
import com.zest.toeic.auth.model.User;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - Users", description = "User Management")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Danh sách users (search, filter, phân trang)")
    public ResponseEntity<ApiResponse<Page<User>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.listUsers(search, status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết user")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUser(id)));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Tạm khóa user")
    public ResponseEntity<ApiResponse<User>> suspendUser(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason") : "No reason";
        return ResponseEntity.ok(ApiResponse.success(adminUserService.suspendUser(id, reason)));
    }

    @PutMapping("/{id}/ban")
    @Operation(summary = "Ban user vĩnh viễn")
    public ResponseEntity<ApiResponse<User>> banUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.banUser(id)));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Kích hoạt lại user")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.activateUser(id)));
    }
}
