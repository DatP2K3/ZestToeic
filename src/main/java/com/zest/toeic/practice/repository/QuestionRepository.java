package com.zest.toeic.practice.repository;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.shared.model.enums.QuestionDifficulty;
import com.zest.toeic.shared.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByPartAndStatus(int part, QuestionStatus status);

    List<Question> findByPartAndDifficultyAndStatus(int part, QuestionDifficulty difficulty, QuestionStatus status);

    long countByStatus(QuestionStatus status);

    long countByPartAndStatus(int part, QuestionStatus status);

    Page<Question> findByPartAndStatus(int part, QuestionStatus status, Pageable pageable);

    Page<Question> findByPart(int part, Pageable pageable);

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    boolean existsByContentIgnoreCase(String content);

    List<Question> findByStatus(QuestionStatus status);
}
