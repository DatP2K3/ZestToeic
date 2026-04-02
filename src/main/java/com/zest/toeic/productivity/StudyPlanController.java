package com.zest.toeic.productivity;

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
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<StudyPlan>> generatePlan(
            @RequestBody Map<String, Object> request, Authentication auth) {
        double weeklyHours = ((Number) request.getOrDefault("weeklyHours", 8.0)).doubleValue();
        List<String> focusAreas = (List<String>) request.getOrDefault("focusAreas", List.of("Part 5", "Part 3"));
        StudyPlan plan = studyPlanService.generatePlan(auth.getName(), weeklyHours, focusAreas);
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
