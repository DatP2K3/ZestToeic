package com.zest.toeic.admin.service;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminQuestionService {

    private final QuestionRepository questionRepository;

    public AdminQuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Page<Question> listQuestions(Integer part, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (part != null && status != null) {
            return questionRepository.findByPartAndStatus(part, status, pageable);
        } else if (part != null) {
            return questionRepository.findByPart(part, pageable);
        } else if (status != null) {
            return questionRepository.findByStatus(status, pageable);
        }
        return questionRepository.findAll(pageable);
    }

    public Question createQuestion(Question question) {
        question.setStatus("PENDING");
        return questionRepository.save(question);
    }

    public Question updateQuestion(String id, Question updates) {
        Question existing = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));

        if (updates.getContent() != null) existing.setContent(updates.getContent());
        if (updates.getOptions() != null) existing.setOptions(updates.getOptions());
        if (updates.getCorrectAnswer() != null) existing.setCorrectAnswer(updates.getCorrectAnswer());
        if (updates.getExplanation() != null) existing.setExplanation(updates.getExplanation());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getDifficulty() != null) existing.setDifficulty(updates.getDifficulty());
        if (updates.getPart() > 0) existing.setPart(updates.getPart());

        return questionRepository.save(existing);
    }

    public void deleteQuestion(String id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
    }

    public Question approveQuestion(String id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
        q.setStatus("PUBLISHED");
        return questionRepository.save(q);
    }

    public Question rejectQuestion(String id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
        q.setStatus("REJECTED");
        return questionRepository.save(q);
    }
}
