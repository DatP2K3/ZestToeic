package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.XpTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface XpTransactionRepository extends MongoRepository<XpTransaction, String> {

    Page<XpTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<XpTransaction> findByUserIdAndCreatedAtAfter(String userId, Instant after);

    long countByUserId(String userId);
}
