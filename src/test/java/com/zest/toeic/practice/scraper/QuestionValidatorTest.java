package com.zest.toeic.practice.scraper;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionValidatorTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionValidator questionValidator;

    private Question validQuestion;

    @BeforeEach
    void setUp() {
        validQuestion = Question.builder()
                .content("Valid content")
                .part(5)
                .correctAnswer("B")
                .options(List.of(
                        Question.QuestionOption.builder().label("A").text("opt1").build(),
                        Question.QuestionOption.builder().label("B").text("opt2").build(),
                        Question.QuestionOption.builder().label("C").text("opt3").build(),
                        Question.QuestionOption.builder().label("D").text("opt4").build()
                ))
                .build();
    }

    @Test
    void isValid_Success_ReturnsTrue() {
        assertTrue(questionValidator.isValid(validQuestion));
    }

    @Test
    void isValid_NullContent_ReturnsFalse() {
        validQuestion.setContent(null);
        assertFalse(questionValidator.isValid(validQuestion));

        validQuestion.setContent("   ");
        assertFalse(questionValidator.isValid(validQuestion));
    }

    @Test
    void isValid_InvalidOptionsSize_ReturnsFalse() {
        validQuestion.setOptions(List.of(
                Question.QuestionOption.builder().label("A").text("opt1").build()
        ));
        assertFalse(questionValidator.isValid(validQuestion));

        validQuestion.setOptions(null);
        assertFalse(questionValidator.isValid(validQuestion));
    }

    @Test
    void isValid_InvalidCorrectAnswer_ReturnsFalse() {
        validQuestion.setCorrectAnswer(null);
        assertFalse(questionValidator.isValid(validQuestion));

        validQuestion.setCorrectAnswer("E");
        assertFalse(questionValidator.isValid(validQuestion));
    }

    @Test
    void isValid_InvalidPart_ReturnsFalse() {
        validQuestion.setPart(0);
        assertFalse(questionValidator.isValid(validQuestion));

        validQuestion.setPart(8);
        assertFalse(questionValidator.isValid(validQuestion));
    }

    @Test
    void isValid_EmptyOptionText_ReturnsFalse() {
        validQuestion.getOptions().get(0).setText("");
        assertFalse(questionValidator.isValid(validQuestion));
    }

    @Test
    void isDuplicate_MatchFound_ReturnsTrue() {
        when(questionRepository.existsByContentIgnoreCase("Valid content")).thenReturn(true);
        assertTrue(questionValidator.isDuplicate(validQuestion, questionRepository));
    }

    @Test
    void isDuplicate_NoMatchFound_ReturnsFalse() {
        when(questionRepository.existsByContentIgnoreCase("Valid content")).thenReturn(false);
        assertFalse(questionValidator.isDuplicate(validQuestion, questionRepository));
    }
}
