package com.zest.toeic.practice.service;

import com.zest.toeic.shared.event.XpAwardedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.zest.toeic.practice.dto.CreateFlashcardRequest;
import com.zest.toeic.practice.dto.FlashcardStats;
import com.zest.toeic.practice.model.Flashcard;
import com.zest.toeic.practice.repository.FlashcardRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.FlashcardStatus;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class FlashcardService {

    private static final Logger log = LoggerFactory.getLogger(FlashcardService.class);

    private final FlashcardRepository flashcardRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FlashcardService(FlashcardRepository flashcardRepository, ApplicationEventPublisher eventPublisher) {
        this.flashcardRepository = flashcardRepository;
        this.eventPublisher = eventPublisher;
    }

    public Flashcard createFlashcard(String userId, CreateFlashcardRequest request) {
        Flashcard card = Flashcard.builder()
                .userId(userId)
                .front(request.getFront())
                .back(request.getBack())
                .tags(request.getTags())
                .part(request.getPart())
                .difficulty(request.getDifficulty() != null ? QuestionDifficulty.valueOf(request.getDifficulty()) : QuestionDifficulty.MEDIUM)
                .easeFactor(2.5)
                .interval(0)
                .repetitions(0)
                .status(FlashcardStatus.LEARNING)
                .nextReviewAt(Instant.now())
                .build();

        return flashcardRepository.save(card);
    }

    public List<Flashcard> getDueCards(String userId, int limit) {
        List<Flashcard> due = flashcardRepository
                .findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(userId, Instant.now());
        return due.stream().limit(limit).toList();
    }

    /**
     * SM-2 Algorithm review.
     * @param quality 0-5 (0=complete blackout, 5=perfect recall)
     */
    public Flashcard reviewCard(String userId, String cardId, int quality) {
        Flashcard card = flashcardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        quality = Math.max(0, Math.min(5, quality));

        if (quality < 3) {
            // Failed — reset
            card.setRepetitions(0);
            card.setInterval(1);
            card.setStatus(FlashcardStatus.LEARNING);
        } else {
            // Passed — advance
            card.setRepetitions(card.getRepetitions() + 1);

            if (card.getRepetitions() == 1) {
                card.setInterval(1);
            } else if (card.getRepetitions() == 2) {
                card.setInterval(6);
            } else {
                card.setInterval((int) Math.round(card.getInterval() * card.getEaseFactor()));
            }

            // Update ease factor
            double newEF = card.getEaseFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            card.setEaseFactor(Math.max(1.3, newEF));

            // Update status
            if (card.getRepetitions() >= 5 && card.getEaseFactor() >= 2.2) {
                card.setStatus(FlashcardStatus.MASTERED);
            } else {
                card.setStatus(FlashcardStatus.REVIEW);
            }
        }

        card.setNextReviewAt(Instant.now().plus(card.getInterval(), ChronoUnit.DAYS));
        card.setLastReviewedAt(Instant.now());

        flashcardRepository.save(card);

        // Award XP for reviewing
        eventPublisher.publishEvent(new XpAwardedEvent(userId, 5, "FLASHCARD_REVIEW", cardId, "Flashcard review XP"));

        log.debug("Card {} reviewed: q={}, interval={}, EF={}, status={}",
                cardId, quality, card.getInterval(), card.getEaseFactor(), card.getStatus());

        return card;
    }

    public FlashcardStats getStats(String userId) {
        long dueNow = flashcardRepository
                .findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(userId, Instant.now()).size();

        return FlashcardStats.builder()
                .total(flashcardRepository.countByUserId(userId))
                .learning(flashcardRepository.countByUserIdAndStatus(userId, FlashcardStatus.LEARNING.name()))
                .review(flashcardRepository.countByUserIdAndStatus(userId, FlashcardStatus.REVIEW.name()))
                .mastered(flashcardRepository.countByUserIdAndStatus(userId, FlashcardStatus.MASTERED.name()))
                .dueNow(dueNow)
                .build();
    }

    public void deleteFlashcard(String userId, String cardId) {
        Flashcard card = flashcardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        flashcardRepository.delete(card);
    }
}
