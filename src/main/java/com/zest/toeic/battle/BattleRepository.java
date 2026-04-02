package com.zest.toeic.battle;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BattleRepository extends MongoRepository<Battle, String> {
    List<Battle> findByStatusInOrderByScheduledAtDesc(List<String> statuses);
}
