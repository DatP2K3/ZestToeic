package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.TestSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends MongoRepository<TestSession, String> {

    Page<TestSession> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, String type, Pageable pageable);

    Optional<TestSession> findByIdAndUserId(String id, String userId);

    Optional<TestSession> findByUserIdAndTypeAndStatus(String userId, String type, String status);

    long countByUserIdAndType(String userId, String type);

    List<TestSession> findByUserIdAndTypeAndStatusAndCreatedAtAfter(
            String userId, String type, String status, Instant after);
}
