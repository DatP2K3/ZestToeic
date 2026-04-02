package com.zest.toeic.admin.featureflag;

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
@Tag(name = "Feature Flags", description = "Feature flag management & checking")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    // ═══════ Admin CRUD ═══════

    @GetMapping("/api/v1/admin/feature-flags")
    @Operation(summary = "List all feature flags")
    public ResponseEntity<ApiResponse<List<FeatureFlag>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.getAll()));
    }

    @PostMapping("/api/v1/admin/feature-flags")
    @Operation(summary = "Create a feature flag")
    public ResponseEntity<ApiResponse<FeatureFlag>> create(@RequestBody FeatureFlag flag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(featureFlagService.create(flag)));
    }

    @PutMapping("/api/v1/admin/feature-flags/{id}")
    @Operation(summary = "Update a feature flag")
    public ResponseEntity<ApiResponse<FeatureFlag>> update(@PathVariable String id, @RequestBody FeatureFlag flag) {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.update(id, flag)));
    }

    @DeleteMapping("/api/v1/admin/feature-flags/{id}")
    @Operation(summary = "Delete a feature flag")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        featureFlagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ═══════ Public Check ═══════

    @GetMapping("/api/v1/features/{name}/check")
    @Operation(summary = "Check if a feature is enabled for current user")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFeature(
            @PathVariable String name, Authentication auth) {
        boolean enabled = featureFlagService.isEnabled(name, auth.getName());
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", enabled)));
    }
}
