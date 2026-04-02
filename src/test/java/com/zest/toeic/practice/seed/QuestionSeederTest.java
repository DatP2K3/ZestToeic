package com.zest.toeic.practice.seed;

import com.zest.toeic.practice.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionSeederTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionSeeder questionSeeder;

    @Test
    void run_AlreadySeeded_DoesNothing() {
        when(questionRepository.countByStatus("PUBLISHED")).thenReturn(5L);

        questionSeeder.run();

        verify(questionRepository, never()).saveAll(any());
    }

    @Test
    void run_NotSeeded_SeedsQuestions() {
        when(questionRepository.countByStatus("PUBLISHED")).thenReturn(0L);

        questionSeeder.run();

        // 60 + 40 + 30 + 30 + 10 + 15 + 15 = 200
        verify(questionRepository).saveAll(argThat(list -> ((List<?>) list).size() == 200));
    }
}
