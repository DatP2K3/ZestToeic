package com.zest.toeic.practice.scraper;

public interface QuestionScraper {
    ScrapingResult scrape(String url, int maxQuestions);
    String getSourceName();
    boolean supports(String url);
}
