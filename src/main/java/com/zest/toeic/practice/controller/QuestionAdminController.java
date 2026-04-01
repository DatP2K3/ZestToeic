package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.dto.ApiResponse;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Questions", description = "Question bank management (Admin only)")
public class QuestionAdminController {

    private final QuestionRepository questionRepository;

    public QuestionAdminController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    @Operation(summary = "List all questions")
    public ResponseEntity<ApiResponse<List<Question>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(questionRepository.findAll()));
    }

    @PostMapping
    @Operation(summary = "Create a new question")
    public ResponseEntity<ApiResponse<Question>> create(@RequestBody Question question) {
        question.setId(null);
        Question saved = questionRepository.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question")
    public ResponseEntity<ApiResponse<Question>> update(@PathVariable String id, @RequestBody Question question) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found: " + id);
        }
        question.setId(id);
        return ResponseEntity.ok(ApiResponse.success(questionRepository.save(question)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Question bank statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        Map<String, Object> stats = Map.of(
                "total", questionRepository.count(),
                "published", questionRepository.countByStatus("PUBLISHED"),
                "part1", questionRepository.countByPartAndStatus(1, "PUBLISHED"),
                "part2", questionRepository.countByPartAndStatus(2, "PUBLISHED"),
                "part3", questionRepository.countByPartAndStatus(3, "PUBLISHED"),
                "part4", questionRepository.countByPartAndStatus(4, "PUBLISHED"),
                "part5", questionRepository.countByPartAndStatus(5, "PUBLISHED"),
                "part6", questionRepository.countByPartAndStatus(6, "PUBLISHED"),
                "part7", questionRepository.countByPartAndStatus(7, "PUBLISHED")
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
