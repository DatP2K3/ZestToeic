package com.zest.toeic.practice.controller;

import com.zest.toeic.shared.dto.ApiResponse;
import com.zest.toeic.shared.scraper.Study4Scraper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Content Pipeline", description = "Scraping & importing questions")
public class QuestionImportController {

    private final Study4Scraper study4Scraper;

    public QuestionImportController(Study4Scraper study4Scraper) {
        this.study4Scraper = study4Scraper;
    }

    @PostMapping("/scrape")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Scrape câu hỏi từ Study4 (Admin only)")
    public ResponseEntity<ApiResponse<Study4Scraper.ScrapingResult>> scrapeStudy4(
            @RequestParam String url,
            @RequestParam(defaultValue = "50") int maxQuestions) {
        Study4Scraper.ScrapingResult result = study4Scraper.scrape(url, maxQuestions);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
