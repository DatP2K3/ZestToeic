package com.zest.toeic.admin.service;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminQuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private AdminQuestionService adminQuestionService;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        mockQuestion = Question.builder()
                .part(5)
                .content("Test question")
                .status("PENDING")
                .build();
        mockQuestion.setId("q1");
    }

    @Test
    void listQuestions_withBothFilters() {
        Page<Question> page = new PageImpl<>(List.of(mockQuestion));
        when(questionRepository.findByPartAndStatus(eq(5), eq("PENDING"), any(PageRequest.class))).thenReturn(page);

        Page<Question> result = adminQuestionService.listQuestions(5, "PENDING", 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listQuestions_withPartFilterOnly() {
        Page<Question> page = new PageImpl<>(List.of(mockQuestion));
        when(questionRepository.findByPart(eq(5), any(PageRequest.class))).thenReturn(page);

        Page<Question> result = adminQuestionService.listQuestions(5, null, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listQuestions_withStatusFilterOnly() {
        Page<Question> page = new PageImpl<>(List.of(mockQuestion));
        when(questionRepository.findByStatus(eq("PENDING"), any(PageRequest.class))).thenReturn(page);

        Page<Question> result = adminQuestionService.listQuestions(null, "PENDING", 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listQuestions_noFilters() {
        Page<Question> page = new PageImpl<>(List.of(mockQuestion));
        when(questionRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Question> result = adminQuestionService.listQuestions(null, null, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void createQuestion_shouldSetStatusPending() {
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        Question newQ = Question.builder().content("New Q").build();
        Question result = adminQuestionService.createQuestion(newQ);

        assertEquals("PENDING", result.getStatus());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void updateQuestion_shouldApplyUpdates() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        Question updates = Question.builder().content("Updated").part(6).build();
        Question result = adminQuestionService.updateQuestion("q1", updates);

        assertEquals("Updated", result.getContent());
        assertEquals(6, result.getPart());
    }

    @Test
    void updateQuestion_shouldThrowNotFound() {
        when(questionRepository.findById("q1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminQuestionService.updateQuestion("q1", new Question()));
    }

    @Test
    void deleteQuestion_shouldDeleteIfExtists() {
        when(questionRepository.existsById("q1")).thenReturn(true);
        adminQuestionService.deleteQuestion("q1");
        verify(questionRepository).deleteById("q1");
    }

    @Test
    void approveQuestion_shouldSetPublished() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        Question result = adminQuestionService.approveQuestion("q1");
        assertEquals("PUBLISHED", result.getStatus());
    }

    @Test
    void rejectQuestion_shouldSetRejected() {
        when(questionRepository.findById("q1")).thenReturn(Optional.of(mockQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        Question result = adminQuestionService.rejectQuestion("q1");
        assertEquals("REJECTED", result.getStatus());
    }
}
