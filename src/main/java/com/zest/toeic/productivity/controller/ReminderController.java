package com.zest.toeic.productivity.controller;
import com.zest.toeic.productivity.model.Reminder;
import com.zest.toeic.productivity.service.ReminderService;

import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminders")
@Tag(name = "Reminders", description = "Scheduled notification reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    @Operation(summary = "List user reminders")
    public ResponseEntity<ApiResponse<List<Reminder>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getUserReminders(auth.getName())));
    }

    @PostMapping
    @Operation(summary = "Create a reminder")
    public ResponseEntity<ApiResponse<Reminder>> create(@RequestBody Reminder reminder, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reminderService.create(auth.getName(), reminder)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reminder")
    public ResponseEntity<ApiResponse<Reminder>> update(@PathVariable String id, @RequestBody Reminder reminder) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.update(id, reminder)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reminder")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        reminderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
