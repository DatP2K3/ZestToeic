package com.zest.toeic.monetization.repository;

import com.zest.toeic.monetization.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByUserIdAndStatus(String userId, String status);
    Optional<Subscription> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
