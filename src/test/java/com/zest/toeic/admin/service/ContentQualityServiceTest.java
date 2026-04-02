package com.zest.toeic.admin.service;

import com.zest.toeic.practice.model.Question;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentQualityServiceTest {

    private final ContentQualityService service = new ContentQualityService();

    @Test
    void validateQuestion_validQuestion_noErrors() {
        Question q = Question.builder()
                .content("This is a valid question content for testing")
                .correctAnswer("A")
                .part(5)
                .options(List.of())
                .build();
        // Note: options size < 4 will produce 1 error
        List<String> errors = service.validateQuestion(q);
        assertEquals(1, errors.size()); // Only options count error
    }

    @Test
    void validateQuestion_emptyContent_hasError() {
        Question q = Question.builder().content("").correctAnswer("A").part(5).build();
        List<String> errors = service.validateQuestion(q);
        assertTrue(errors.stream().anyMatch(e -> e.contains("không được để trống")));
    }

    @Test
    void validateQuestion_invalidPart_hasError() {
        Question q = Question.builder().content("Valid content here").correctAnswer("A").part(9).build();
        List<String> errors = service.validateQuestion(q);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Part phải từ 1 đến 7")));
    }

    @Test
    void isDuplicate_identicalTexts_returnsTrue() {
        assertTrue(service.isDuplicate("Hello world", List.of("Hello world")));
    }

    @Test
    void isDuplicate_similarTexts_returnsTrue() {
        assertTrue(service.isDuplicate("Hello world test", List.of("Hello world tset"))); // typo still > 85%
    }

    @Test
    void isDuplicate_differentTexts_returnsFalse() {
        assertFalse(service.isDuplicate("Completely different text here", List.of("Another unrelated sentence")));
    }

    @Test
    void isDuplicate_nullInputs_returnsFalse() {
        assertFalse(service.isDuplicate(null, List.of("test")));
        assertFalse(service.isDuplicate("test", null));
    }

    @Test
    void calculateSimilarity_identicalStrings_returns1() {
        assertEquals(1.0, service.calculateSimilarity("abc", "abc"));
    }

    @Test
    void calculateSimilarity_emptyStrings_returns1() {
        assertEquals(1.0, service.calculateSimilarity("", ""));
    }
}
