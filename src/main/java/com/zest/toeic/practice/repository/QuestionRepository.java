package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByPartAndStatus(int part, String status);

    List<Question> findByPartAndDifficultyAndStatus(int part, String difficulty, String status);

    long countByStatus(String status);

    long countByPartAndStatus(int part, String status);

    Page<Question> findByPartAndStatus(int part, String status, Pageable pageable);

    Page<Question> findByPart(int part, Pageable pageable);

    Page<Question> findByStatus(String status, Pageable pageable);
}
