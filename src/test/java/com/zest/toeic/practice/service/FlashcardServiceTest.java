package com.zest.toeic.practice.service;

import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.shared.event.XpAwardedEvent;
import com.zest.toeic.practice.dto.CreateFlashcardRequest;
import com.zest.toeic.practice.dto.FlashcardStats;
import com.zest.toeic.practice.model.Flashcard;
import com.zest.toeic.practice.repository.FlashcardRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.FlashcardStatus;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FlashcardService flashcardService;

    private Flashcard mockCard;

    @BeforeEach
    void setUp() {
        mockCard = Flashcard.builder()
                .userId("user1")
                .front("abandon")
                .back("thay đổi")
                .part(5)
                .difficulty(QuestionDifficulty.EASY)
                .easeFactor(2.5)
                .interval(0)
                .repetitions(0)
                .status(FlashcardStatus.LEARNING)
                .build();
        mockCard.setId("c1");
    }

    @Test
    void createFlashcard_Success() {
        CreateFlashcardRequest req = new CreateFlashcardRequest();
        req.setFront("abandon");
        req.setBack("thay đổi");
        req.setPart(5);
        req.setDifficulty("EASY");

        when(flashcardRepository.save(any(Flashcard.class))).thenAnswer(i -> i.getArguments()[0]);

        Flashcard result = flashcardService.createFlashcard("user1", req);

        assertEquals("user1", result.getUserId());
        assertEquals("abandon", result.getFront());
        assertEquals(FlashcardStatus.LEARNING, result.getStatus());
        assertEquals(2.5, result.getEaseFactor());
    }

    @Test
    void getDueCards_ReturnsCards() {
        when(flashcardRepository.findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(eq("user1"), any(Instant.class)))
                .thenReturn(List.of(mockCard));

        List<Flashcard> due = flashcardService.getDueCards("user1", 10);

        assertEquals(1, due.size());
    }

    @Test
    void reviewCard_Quality0To2_ResetsCard() {
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.of(mockCard));
        mockCard.setRepetitions(2); // simulate progressed card

        Flashcard result = flashcardService.reviewCard("user1", "c1", 2);

        assertEquals(0, result.getRepetitions());
        assertEquals(1, result.getInterval());
        assertEquals(FlashcardStatus.LEARNING, result.getStatus());
        
        verify(flashcardRepository).save(mockCard);
        verify(eventPublisher).publishEvent(any(XpAwardedEvent.class));
    }

    @Test
    void reviewCard_Quality3To5_Repetition1_Interval1() {
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.of(mockCard));

        Flashcard result = flashcardService.reviewCard("user1", "c1", 4);

        assertEquals(1, result.getRepetitions());
        assertEquals(1, result.getInterval());
        assertEquals(FlashcardStatus.REVIEW, result.getStatus());
        assertEquals(2.5, result.getEaseFactor(), 0.001); // EF logic: 2.5 + (0.1 - 1*(0.1)) = 2.5
    }

    @Test
    void reviewCard_Quality3To5_Repetition2_Interval6() {
        mockCard.setRepetitions(1);
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.of(mockCard));

        Flashcard result = flashcardService.reviewCard("user1", "c1", 5);

        assertEquals(2, result.getRepetitions());
        assertEquals(6, result.getInterval());
    }

    @Test
    void reviewCard_MasteredStatus() {
        mockCard.setRepetitions(4);
        mockCard.setEaseFactor(2.5);
        mockCard.setInterval(6);
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.of(mockCard));

        Flashcard result = flashcardService.reviewCard("user1", "c1", 5);

        assertEquals(5, result.getRepetitions());
        assertEquals(FlashcardStatus.MASTERED, result.getStatus());
        assertEquals(15, result.getInterval()); // 6 * 2.5
    }

    @Test
    void reviewCard_NotFound_ThrowsException() {
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> flashcardService.reviewCard("user1", "c1", 5));
    }

    @Test
    void getStats_ReturnsCorrectStats() {
        when(flashcardRepository.countByUserId("user1")).thenReturn(10L);
        when(flashcardRepository.countByUserIdAndStatus("user1", "LEARNING")).thenReturn(5L);
        when(flashcardRepository.countByUserIdAndStatus("user1", "REVIEW")).thenReturn(3L);
        when(flashcardRepository.countByUserIdAndStatus("user1", "MASTERED")).thenReturn(2L);
        when(flashcardRepository.findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(eq("user1"), any(Instant.class)))
                .thenReturn(List.of(mockCard, mockCard)); // 2 due

        FlashcardStats stats = flashcardService.getStats("user1");

        assertEquals(10L, stats.getTotal());
        assertEquals(5L, stats.getLearning());
        assertEquals(3L, stats.getReview());
        assertEquals(2L, stats.getMastered());
        assertEquals(2L, stats.getDueNow());
    }

    @Test
    void deleteFlashcard_Found_Deletes() {
        when(flashcardRepository.findByIdAndUserId("c1", "user1")).thenReturn(Optional.of(mockCard));
        flashcardService.deleteFlashcard("user1", "c1");
        verify(flashcardRepository).delete(mockCard);
    }
}
