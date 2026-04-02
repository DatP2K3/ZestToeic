package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.UserAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface UserAnswerRepository extends MongoRepository<UserAnswer, String> {

    List<UserAnswer> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<UserAnswer> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<UserAnswer> findByUserIdAndTestId(String userId, String testId);

    long countByUserIdAndCorrect(String userId, boolean correct);

    long countByUserId(String userId);

    long countByUserIdAndCreatedAtBetween(String userId, Instant start, Instant end);
}
