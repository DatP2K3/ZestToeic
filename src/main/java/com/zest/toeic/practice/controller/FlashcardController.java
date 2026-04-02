package com.zest.toeic.practice.controller;

import com.zest.toeic.practice.dto.CreateFlashcardRequest;
import com.zest.toeic.practice.dto.FlashcardStats;
import com.zest.toeic.practice.model.Flashcard;
import com.zest.toeic.practice.service.FlashcardService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
@Tag(name = "Flashcards", description = "Spaced Repetition flashcard system (SM-2)")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping
    @Operation(summary = "Tạo flashcard mới")
    public ResponseEntity<ApiResponse<Flashcard>> create(
            @Valid @RequestBody CreateFlashcardRequest request, Authentication auth) {
        Flashcard card = flashcardService.createFlashcard(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(card));
    }

    @GetMapping("/due")
    @Operation(summary = "Lấy flashcards cần ôn tập (due for review)")
    public ResponseEntity<ApiResponse<List<Flashcard>>> getDueCards(
            @RequestParam(defaultValue = "20") int limit, Authentication auth) {
        List<Flashcard> cards = flashcardService.getDueCards(auth.getName(), limit);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Review flashcard — SM-2 algorithm (quality 0-5)")
    public ResponseEntity<ApiResponse<Flashcard>> review(
            @PathVariable String id,
            @RequestParam int quality,
            Authentication auth) {
        Flashcard card = flashcardService.reviewCard(auth.getName(), id, quality);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/stats")
    @Operation(summary = "Flashcard statistics")
    public ResponseEntity<ApiResponse<FlashcardStats>> getStats(Authentication auth) {
        FlashcardStats stats = flashcardService.getStats(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa flashcard")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication auth) {
        flashcardService.deleteFlashcard(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
