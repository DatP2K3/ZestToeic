package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends MongoRepository<Badge, String> {
    Optional<Badge> findByCriteria(String criteria);
    List<Badge> findByActiveTrue();
    List<Badge> findByCategory(String category);
}
