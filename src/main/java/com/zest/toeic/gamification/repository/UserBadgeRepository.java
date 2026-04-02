package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.UserBadge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends MongoRepository<UserBadge, String> {
    List<UserBadge> findByUserId(String userId);
    Optional<UserBadge> findByUserIdAndBadgeId(String userId, String badgeId);
    boolean existsByUserIdAndBadgeId(String userId, String badgeId);
}
