package com.zest.toeic.practice.controller;

import com.zest.toeic.shared.dto.ApiResponse;
import com.zest.toeic.practice.scraper.ScraperService;
import com.zest.toeic.practice.scraper.ScrapingResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin - Content Pipeline", description = "Scraping & importing questions")
public class QuestionImportController {

    private final ScraperService scraperService;

    public QuestionImportController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/scrape")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Scrape câu hỏi từ source tuỳ url (Admin only)")
    public ResponseEntity<ApiResponse<ScrapingResult>> scrapeQuestionSource(
            @RequestParam String url,
            @RequestParam(defaultValue = "50") int maxQuestions) {
        ScrapingResult result = scraperService.scrapeFromUrl(url, maxQuestions);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
