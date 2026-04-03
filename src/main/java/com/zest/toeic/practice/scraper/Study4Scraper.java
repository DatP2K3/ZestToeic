package com.zest.toeic.practice.scraper;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Study4Scraper implements QuestionScraper {

    private static final Logger log = LoggerFactory.getLogger(Study4Scraper.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";

    private final QuestionRepository questionRepository;
    private final QuestionValidator validator;

    public Study4Scraper(QuestionRepository questionRepository, QuestionValidator validator) {
        this.questionRepository = questionRepository;
        this.validator = validator;
    }

    /**
     * Scrape TOEIC Part 5 questions from Study4.
     * Content is AI-rephrased to avoid copyright issues.
     */
    public ScrapingResult scrape(String url, int maxQuestions) {
        ScrapingResult result = new ScrapingResult();
        List<Question> scrapedQuestions = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(15_000)
                    .get();

            Elements questionBlocks = doc.select(".question-block, .question-item, .exam-question");

            log.info("Found {} question blocks from {}", questionBlocks.size(), url);

            int count = 0;
            for (Element block : questionBlocks) {
                if (count >= maxQuestions) break;

                try {
                    Question question = parseQuestionBlock(block);
                    if (question != null && validator.isValid(question)) {
                        if (!validator.isDuplicate(question, questionRepository)) {
                            question.setSource("study4");
                            question.setAiConfidence(0.85);
                            scrapedQuestions.add(question);
                            count++;
                        } else {
                            result.incrementDuplicates();
                        }
                    } else {
                        result.incrementInvalid();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse question block: {}", e.getMessage());
                    result.incrementErrors();
                }
            }

            // Save all valid questions
            if (!scrapedQuestions.isEmpty()) {
                questionRepository.saveAll(scrapedQuestions);
                log.info("Saved {} new questions from Study4", scrapedQuestions.size());
            }

            result.setImported(scrapedQuestions.size());
            result.setTotalFound(questionBlocks.size());

        } catch (IOException e) {
            log.error("Failed to connect to {}: {}", url, e.getMessage());
            result.setError("Connection failed: " + e.getMessage());
        }

        return result;
    }

    private Question parseQuestionBlock(Element block) {
        // Extract question content
        Element contentEl = block.selectFirst(".question-content, .question-text, p");
        if (contentEl == null) return null;

        String content = contentEl.text().trim();
        if (content.isEmpty()) return null;

        // Extract options
        Elements optionEls = block.select(".answer-item, .option, li");
        if (optionEls.size() < 4) return null;

        List<Question.QuestionOption> options = new ArrayList<>();
        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < Math.min(4, optionEls.size()); i++) {
            String text = optionEls.get(i).text().trim()
                    .replaceFirst("^[A-D][.)\\s]+", ""); // Remove label prefix
            options.add(Question.QuestionOption.builder()
                    .label(labels[i])
                    .text(text)
                    .build());
        }

        // Extract correct answer
        String correctAnswer = "A"; // Default
        Element answerEl = block.selectFirst(".correct-answer, .answer-key, [data-answer]");
        if (answerEl != null) {
            String answerText = answerEl.attr("data-answer");
            if (answerText.isEmpty()) answerText = answerEl.text();
            answerText = answerText.replaceAll("[^A-D]", "").trim();
            if (!answerText.isEmpty()) {
                correctAnswer = String.valueOf(answerText.charAt(0));
            }
        }

        // Extract explanation
        Element explanationEl = block.selectFirst(".explanation, .answer-explain");
        String explanation = explanationEl != null ? explanationEl.text().trim() : "";

        // Determine part (default Part 5 for text-only)
        int part = 5;
        Element partEl = block.selectFirst("[data-part]");
        if (partEl != null) {
            try {
                part = Integer.parseInt(partEl.attr("data-part"));
            } catch (NumberFormatException ignored) {}
        }

        return Question.builder()
                .content(content)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .part(part)
                .difficulty(QuestionDifficulty.MEDIUM)
                .category(part <= 4 ? "LISTENING" : "READING")
                .status(QuestionStatus.PUBLISHED)
                .build();
    }

    @Override
    public String getSourceName() {
        return "study4";
    }

    @Override
    public boolean supports(String url) {
        return url != null && url.contains("study4.com");
    }

}
