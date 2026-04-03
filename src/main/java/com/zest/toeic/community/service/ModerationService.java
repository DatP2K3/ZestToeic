package com.zest.toeic.community.service;

import com.zest.toeic.community.model.ModerationAction;
import com.zest.toeic.community.repository.ModerationActionRepository;
import com.zest.toeic.shared.ai.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final ModerationActionRepository moderationActionRepository;
    private final GeminiClient geminiClient;

    // Regex banned words (Vietnamese common profanity + spam patterns)
    private static final List<String> BANNED_PATTERNS = List.of(
            "(?i)\\b(đ[iíìỉĩị]t|l[oòóỏõọ]n|c[aáàảãạ]c|đ[uùúủũụ]|ch[oòóỏõọ] ch[eèéẻẽẹ]t|ngu)\\b",
            "(?i)(spam|scam|hack|cheat)",
            "(?i)(http[s]?://\\S+){3,}" // 3+ URLs = likely spam
    );

    private static final List<Pattern> COMPILED_PATTERNS = BANNED_PATTERNS.stream()
            .map(Pattern::compile)
            .toList();

    public ModerationService(ModerationActionRepository moderationActionRepository, GeminiClient geminiClient) {
        this.moderationActionRepository = moderationActionRepository;
        this.geminiClient = geminiClient;
    }

    /**
     * Check content for toxicity. Returns: "CLEAN", "REGEX_FLAGGED", "AI_FLAGGED"
     */
    public String checkContent(String content, String authorId) {
        // Layer 1: Regex filter (fast, free)
        for (Pattern pattern : COMPILED_PATTERNS) {
            if (pattern.matcher(content).find()) {
                logAction(null, "CONTENT", "HIDE", "Regex filter match", authorId, "REGEX", 1.0);
                log.warn("🚫 Regex flagged content from user {}", authorId);
                return "REGEX_FLAGGED";
            }
        }

        // Layer 2: AI toxicity detection (smart, catches evasion)
        try {
            double toxicityScore = analyzeWithAI(content);
            if (toxicityScore >= 0.7) {
                logAction(null, "CONTENT", "HIDE", "AI toxicity score: " + toxicityScore, authorId, "AI", toxicityScore);
                log.warn("🤖 AI flagged content from user {} (score: {})", authorId, toxicityScore);
                return "AI_FLAGGED";
            }
        } catch (Exception e) {
            log.debug("AI moderation unavailable, relying on regex only: {}", e.getMessage());
        }

        return "CLEAN";
    }

    public long getStrikeCount(String userId) {
        return moderationActionRepository.countByTargetIdAndAction(userId, "STRIKE");
    }

    private double analyzeWithAI(String content) {
        String prompt = String.format(
                "Analyze this Vietnamese text for toxicity. Return ONLY a number between 0.0 and 1.0 " +
                "(0=clean, 1=extremely toxic). No explanation needed. Text: \"%s\"", content);
        try {
            String response = geminiClient.ask(prompt);
            return Double.parseDouble(response.trim());
        } catch (Exception e) {
            return 0.0; // Default to clean if AI fails
        }
    }

    private void logAction(String targetId, String targetType, String action, String reason,
                          String moderatorId, String method, double score) {
        ModerationAction modAction = ModerationAction.builder()
                .targetId(targetId != null ? targetId : moderatorId)
                .targetType(targetType)
                .action(action)
                .reason(reason)
                .moderatorId("SYSTEM")
                .detectionMethod(method)
                .toxicityScore(score)
                .build();
        moderationActionRepository.save(modAction);
    }
}
