package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.DailyQuest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQuestRepository extends MongoRepository<DailyQuest, String> {

    Optional<DailyQuest> findByUserIdAndDate(String userId, LocalDate date);
}
