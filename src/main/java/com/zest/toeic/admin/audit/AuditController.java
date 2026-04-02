package com.zest.toeic.admin.audit;

import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@Tag(name = "Admin - Audit Logs", description = "Audit trail for admin actions")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "List all audit logs (paginated)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAllLogs(page, size)));
    }

    @GetMapping("/admin/{adminId}")
    @Operation(summary = "Logs by admin user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByAdmin(
            @PathVariable String adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getLogsByAdmin(adminId, page, size)));
    }

    @GetMapping("/target/{type}/{targetId}")
    @Operation(summary = "Logs by target resource")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByTarget(
            @PathVariable String type, @PathVariable String targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getLogsByTarget(type, targetId, page, size)));
    }
}
