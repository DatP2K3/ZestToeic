package com.zest.toeic.productivity;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface StudyPlanRepository extends MongoRepository<StudyPlan, String> {
    Optional<StudyPlan> findByUserIdAndStatus(String userId, String status);
}
