package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByPartAndStatus(int part, String status);

    List<Question> findByPartAndDifficultyAndStatus(int part, String difficulty, String status);

    long countByStatus(String status);

    long countByPartAndStatus(int part, String status);
}
