package com.zest.toeic.productivity.controller;
import com.zest.toeic.productivity.model.StudyPlan;
import com.zest.toeic.productivity.service.StudyPlanService;

import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/planner")
@Tag(name = "Study Planner", description = "AI-assisted weekly study planning")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @GetMapping
    @Operation(summary = "Get current active plan")
    public ResponseEntity<ApiResponse<StudyPlan>> getCurrentPlan(Authentication auth) {
        return studyPlanService.getCurrentPlan(auth.getName())
                .map(plan -> ResponseEntity.ok(ApiResponse.success(plan)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a new AI-assisted study plan")
    public ResponseEntity<ApiResponse<StudyPlan>> generatePlan(
            @Valid @RequestBody com.zest.toeic.productivity.dto.GenerateStudyPlanRequest request, Authentication auth) {
        StudyPlan plan = studyPlanService.generatePlan(auth.getName(), request.weeklyHours(), request.focusAreas());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(plan));
    }

    @PutMapping("/{id}/tasks/{day}/{index}/complete")
    @Operation(summary = "Mark a task as complete")
    public ResponseEntity<ApiResponse<StudyPlan>> completeTask(
            @PathVariable String id, @PathVariable int day, @PathVariable int index) {
        return ResponseEntity.ok(ApiResponse.success(studyPlanService.completeTask(id, day, index)));
    }

    @PostMapping("/{id}/adjust")
    @Operation(summary = "Auto-adjust plan for missed tasks")
    public ResponseEntity<ApiResponse<StudyPlan>> adjustPlan(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(studyPlanService.adjustPlan(id)));
    }
}
