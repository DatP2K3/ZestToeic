package com.zest.toeic.admin.service;

import com.zest.toeic.practice.model.Question;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentQualityService {

    /**
     * Validate question format before saving.
     * Returns list of validation errors (empty = valid).
     */
    public List<String> validateQuestion(Question question) {
        java.util.ArrayList<String> errors = new java.util.ArrayList<>();

        if (question.getContent() == null || question.getContent().isBlank()) {
            errors.add("Nội dung câu hỏi không được để trống");
        }
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
            errors.add("Đáp án đúng không được để trống");
        }
        if (question.getOptions() == null || question.getOptions().size() < 4) {
            errors.add("Câu hỏi phải có ít nhất 4 lựa chọn");
        }
        if (question.getPart() < 1 || question.getPart() > 7) {
            errors.add("Part phải từ 1 đến 7");
        }
        if (question.getContent() != null && question.getContent().length() < 10) {
            errors.add("Nội dung câu hỏi phải có ít nhất 10 ký tự");
        }

        return errors;
    }

    /**
     * Check if a question is a near-duplicate using Levenshtein distance.
     */
    public boolean isDuplicate(String newContent, List<String> existingContents) {
        if (newContent == null || existingContents == null) return false;

        String normalizedNew = normalize(newContent);
        for (String existing : existingContents) {
            String normalizedExisting = normalize(existing);
            double similarity = calculateSimilarity(normalizedNew, normalizedExisting);
            if (similarity >= 0.85) {
                return true;
            }
        }
        return false;
    }

    double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9\\sàáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ]", "").trim();
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
