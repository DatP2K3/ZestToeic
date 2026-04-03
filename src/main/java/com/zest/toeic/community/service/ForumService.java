package com.zest.toeic.community.service;

import com.zest.toeic.community.model.ForumComment;
import com.zest.toeic.community.model.ForumPost;
import com.zest.toeic.community.repository.ForumCommentRepository;
import com.zest.toeic.community.repository.ForumPostRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import com.zest.toeic.shared.model.enums.ForumPostStatus;
import com.zest.toeic.shared.model.enums.ForumPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ForumService {

    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ModerationService moderationService;

    public ForumService(ForumPostRepository postRepository,
                        ForumCommentRepository commentRepository,
                        ModerationService moderationService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.moderationService = moderationService;
    }

    // ═══════ Posts ═══════

    public ForumPost createPost(String userId, String userName, String title, String content, String type, List<String> tags) {
        String moderationResult = moderationService.checkContent(content, userId);
        ForumPostStatus status = "CLEAN".equals(moderationResult) ? ForumPostStatus.PUBLISHED : ForumPostStatus.UNDER_REVIEW;

        ForumPost post = ForumPost.builder()
                .title(title)
                .content(content)
                .authorId(userId)
                .authorName(userName)
                .type(type != null ? ForumPostType.valueOf(type) : ForumPostType.DISCUSSION)
                .tags(tags != null ? tags : List.of())
                .status(status)
                .build();
        return postRepository.save(post);
    }

    public Page<ForumPost> getPosts(String type, String tag, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (type != null && tag != null) {
            return postRepository.findByStatusAndType(ForumPostStatus.PUBLISHED.name(), type, pageable);
        }
        if (tag != null) {
            return postRepository.findByStatusAndTagsContaining(ForumPostStatus.PUBLISHED.name(), tag, pageable);
        }
        if (type != null) {
            return postRepository.findByStatusAndType(ForumPostStatus.PUBLISHED.name(), type, pageable);
        }
        return postRepository.findByStatus(ForumPostStatus.PUBLISHED.name(), pageable);
    }

    public ForumPost getPost(String postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    public ForumPost upvotePost(String postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        post.setUpvotes(post.getUpvotes() + 1);
        return postRepository.save(post);
    }

    // ═══════ Comments ═══════

    public ForumComment addComment(String postId, String userId, String userName, String content, String parentId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        String moderationResult = moderationService.checkContent(content, userId);
        ForumPostStatus status = "CLEAN".equals(moderationResult) ? ForumPostStatus.PUBLISHED : ForumPostStatus.UNDER_REVIEW;

        ForumComment comment = ForumComment.builder()
                .postId(postId)
                .parentId(parentId)
                .content(content)
                .authorId(userId)
                .authorName(userName)
                .status(status)
                .build();
        ForumComment saved = commentRepository.save(comment);

        // Update post comment count
        int count = commentRepository.countByPostIdAndStatus(postId, ForumPostStatus.PUBLISHED.name());
        postRepository.findById(postId).ifPresent(p -> {
            p.setCommentCount(count);
            postRepository.save(p);
        });

        return saved;
    }

    public List<ForumComment> getComments(String postId) {
        return commentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(postId, ForumPostStatus.PUBLISHED.name());
    }

    public ForumComment upvoteComment(String commentId) {
        ForumComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        comment.setUpvotes(comment.getUpvotes() + 1);
        return commentRepository.save(comment);
    }

    // ═══════ Q&A: Best Answer ═══════

    public ForumPost markBestAnswer(String postId, String commentId, String userId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (ForumPostType.QA != post.getType()) {
            throw new BadRequestException("Chỉ bài viết dạng Q&A mới có thể đánh dấu câu trả lời hay nhất");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw new BadRequestException("Chỉ người đặt câu hỏi mới được chọn câu trả lời hay nhất");
        }

        // Unmark old best answer
        if (post.getBestAnswerId() != null) {
            commentRepository.findById(post.getBestAnswerId()).ifPresent(old -> {
                old.setBestAnswer(false);
                commentRepository.save(old);
            });
        }

        // Mark new best answer
        ForumComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        comment.setBestAnswer(true);
        commentRepository.save(comment);

        post.setBestAnswerId(commentId);
        post.setResolved(true);
        return postRepository.save(post);
    }
}
