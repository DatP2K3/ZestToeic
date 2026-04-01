package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.dto.AnswerResult;
import com.zest.toeic.practice.dto.SubmitAnswerRequest;
import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.service.PracticeService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Practice", description = "Question practice & answer submission")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/questions/random")
    @Operation(summary = "Lấy câu hỏi random theo Part và Difficulty")
    public ResponseEntity<ApiResponse<List<Question>>> getRandomQuestions(
            @RequestParam(required = false) Integer part,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "10") int limit) {
        List<Question> questions = practiceService.getRandomQuestions(part, difficulty, limit);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @GetMapping("/questions/{id}")
    @Operation(summary = "Lấy chi tiết câu hỏi theo ID")
    public ResponseEntity<ApiResponse<Question>> getQuestion(@PathVariable String id) {
        Question question = practiceService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    @PostMapping("/answers/submit")
    @Operation(summary = "Submit câu trả lời")
    public ResponseEntity<ApiResponse<AnswerResult>> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        AnswerResult result = practiceService.submitAnswer(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
