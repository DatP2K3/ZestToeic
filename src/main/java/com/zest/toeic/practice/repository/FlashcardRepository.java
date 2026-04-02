package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.Flashcard;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FlashcardRepository extends MongoRepository<Flashcard, String> {

    List<Flashcard> findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(String userId, Instant before);

    List<Flashcard> findByUserIdAndStatus(String userId, String status);

    Optional<Flashcard> findByIdAndUserId(String id, String userId);

    long countByUserIdAndStatus(String userId, String status);

    long countByUserId(String userId);
}
