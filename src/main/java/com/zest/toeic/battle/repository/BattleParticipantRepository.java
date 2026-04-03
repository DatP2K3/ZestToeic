package com.zest.toeic.battle.repository;
import com.zest.toeic.battle.model.BattleParticipant;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BattleParticipantRepository extends MongoRepository<BattleParticipant, String> {
    List<BattleParticipant> findByBattleIdOrderByScoreDesc(String battleId);
    Optional<BattleParticipant> findByBattleIdAndUserId(String battleId, String userId);
    long countByBattleId(String battleId);
}
