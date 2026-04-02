package com.zest.toeic.community.repository;

import com.zest.toeic.community.model.ForumComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ForumCommentRepository extends MongoRepository<ForumComment, String> {
    List<ForumComment> findByPostIdAndStatusOrderByCreatedAtAsc(String postId, String status);
    List<ForumComment> findByPostIdAndParentIdIsNullAndStatusOrderByUpvotesDesc(String postId, String status);
    List<ForumComment> findByParentId(String parentId);
    int countByPostIdAndStatus(String postId, String status);
}
