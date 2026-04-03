package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.TestSession;
import com.zest.toeic.shared.model.enums.SessionStatus;
import com.zest.toeic.shared.model.enums.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends MongoRepository<TestSession, String> {

    Page<TestSession> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, TestType type, Pageable pageable);

    Optional<TestSession> findByIdAndUserId(String id, String userId);

    Optional<TestSession> findByUserIdAndTypeAndStatus(String userId, TestType type, SessionStatus status);

    long countByUserIdAndType(String userId, TestType type);

    List<TestSession> findByUserIdAndTypeAndStatusAndCreatedAtAfter(
            String userId, TestType type, SessionStatus status, Instant after);
}
