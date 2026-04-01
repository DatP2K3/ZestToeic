package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.UserAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserAnswerRepository extends MongoRepository<UserAnswer, String> {

    List<UserAnswer> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndCorrect(String userId, boolean correct);

    long countByUserId(String userId);
}
