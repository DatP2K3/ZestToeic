package com.zest.toeic.community.repository;

import com.zest.toeic.community.model.ModerationAction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ModerationActionRepository extends MongoRepository<ModerationAction, String> {
    List<ModerationAction> findByTargetIdAndTargetType(String targetId, String targetType);
    long countByTargetIdAndAction(String targetId, String action);
}
