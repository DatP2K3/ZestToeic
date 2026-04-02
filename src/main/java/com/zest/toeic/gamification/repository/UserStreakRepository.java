package com.zest.toeic.gamification.repository;

import com.zest.toeic.gamification.model.UserStreak;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserStreakRepository extends MongoRepository<UserStreak, String> {

    Optional<UserStreak> findByUserId(String userId);

    List<UserStreak> findByLastActiveDateBefore(LocalDate date);
}
