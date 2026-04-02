package com.zest.toeic.community.repository;

import com.zest.toeic.community.model.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ForumPostRepository extends MongoRepository<ForumPost, String> {
    Page<ForumPost> findByStatusAndType(String status, String type, Pageable pageable);
    Page<ForumPost> findByStatus(String status, Pageable pageable);
    Page<ForumPost> findByAuthorId(String authorId, Pageable pageable);
    Page<ForumPost> findByStatusAndTagsContaining(String status, String tag, Pageable pageable);
    List<ForumPost> findByStatusOrderByUpvotesDesc(String status);
}
