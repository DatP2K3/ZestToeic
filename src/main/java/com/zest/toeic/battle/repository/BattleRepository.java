package com.zest.toeic.battle.repository;
import com.zest.toeic.battle.model.Battle;

import com.zest.toeic.shared.model.enums.BattleStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BattleRepository extends MongoRepository<Battle, String> {
    List<Battle> findByStatusInOrderByScheduledAtDesc(List<BattleStatus> statuses);
}
