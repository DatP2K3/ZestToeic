package com.zest.toeic.practice.scraper;

import com.zest.toeic.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperService {
    private final List<QuestionScraper> scrapers;

    public ScraperService(List<QuestionScraper> scrapers) {
        this.scrapers = scrapers;
    }

    public ScrapingResult scrapeFromUrl(String url, int maxQuestions) {
        return scrapers.stream()
                .filter(s -> s.supports(url))
                .findFirst()
                .map(s -> s.scrape(url, maxQuestions))
                .orElseThrow(() -> new BadRequestException("No scraper found for url: " + url));
    }
}
