package com.zest.toeic.shared.ai.repository;

import com.zest.toeic.shared.ai.model.AIExplanation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AIExplanationRepository extends MongoRepository<AIExplanation, String> {

    Optional<AIExplanation> findByQuestionIdAndUserAnswer(String questionId, String userAnswer);
}
