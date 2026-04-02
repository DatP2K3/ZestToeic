package com.zest.toeic.community.service;

import com.zest.toeic.community.model.ForumComment;
import com.zest.toeic.community.model.ForumPost;
import com.zest.toeic.community.repository.ForumCommentRepository;
import com.zest.toeic.community.repository.ForumPostRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceTest {

    @Mock private ForumPostRepository postRepository;
    @Mock private ForumCommentRepository commentRepository;
    @Mock private ModerationService moderationService;
    @InjectMocks private ForumService forumService;

    @Test
    void createPost_cleanContent_publishesImmediately() {
        when(moderationService.checkContent(anyString(), anyString())).thenReturn("CLEAN");
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumPost post = forumService.createPost("u1", "User1", "Title", "Content", "DISCUSSION", List.of("toeic"));
        assertEquals("PUBLISHED", post.getStatus());
    }

    @Test
    void createPost_flaggedContent_goesToReview() {
        when(moderationService.checkContent(anyString(), anyString())).thenReturn("REGEX_FLAGGED");
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumPost post = forumService.createPost("u1", "User1", "Title", "Bad content", "DISCUSSION", null);
        assertEquals("UNDER_REVIEW", post.getStatus());
    }

    @Test
    void getPost_incrementsViewCount() {
        ForumPost post = ForumPost.builder().title("Test").viewCount(5).build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumPost result = forumService.getPost("p1");
        assertEquals(6, result.getViewCount());
    }

    @Test
    void getPost_throwsWhenNotFound() {
        when(postRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> forumService.getPost("unknown"));
    }

    @Test
    void upvotePost_incrementsUpvotes() {
        ForumPost post = ForumPost.builder().upvotes(3).build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumPost result = forumService.upvotePost("p1");
        assertEquals(4, result.getUpvotes());
    }

    @Test
    void addComment_createsPublishedComment() {
        ForumPost post = ForumPost.builder().build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));
        when(moderationService.checkContent(anyString(), anyString())).thenReturn("CLEAN");
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(commentRepository.countByPostIdAndStatus("p1", "PUBLISHED")).thenReturn(1);

        ForumComment result = forumService.addComment("p1", "u1", "User1", "Nice!", null);
        assertEquals("PUBLISHED", result.getStatus());
    }

    @Test
    void markBestAnswer_worksForQAPost() {
        ForumPost post = ForumPost.builder().type("QA").authorId("u1").build();
        post.setId("p1");
        ForumComment comment = ForumComment.builder().build();
        comment.setId("c1");

        when(postRepository.findById("p1")).thenReturn(Optional.of(post));
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumPost result = forumService.markBestAnswer("p1", "c1", "u1");
        assertTrue(result.isResolved());
        assertEquals("c1", result.getBestAnswerId());
    }

    @Test
    void markBestAnswer_throwsForDiscussionPost() {
        ForumPost post = ForumPost.builder().type("DISCUSSION").authorId("u1").build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));

        assertThrows(BadRequestException.class, () -> forumService.markBestAnswer("p1", "c1", "u1"));
    }

    @Test
    void markBestAnswer_throwsForNonAuthor() {
        ForumPost post = ForumPost.builder().type("QA").authorId("u1").build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));

        assertThrows(BadRequestException.class, () -> forumService.markBestAnswer("p1", "c1", "otherUser"));
    }

    @Test
    void getPosts_callsRepository() {
        when(postRepository.findByStatus(anyString(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Page<ForumPost> result = forumService.getPosts(null, null, 0, 10);
        assertNotNull(result);
    }
    
    @Test
    void getPosts_withTypeAndTag_callsRepository() {
        when(postRepository.findByStatusAndType(anyString(), anyString(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Page<ForumPost> result = forumService.getPosts("DISCUSSION", "toeic", 0, 10);
        assertNotNull(result);
    }

    @Test
    void getComments_callsRepository() {
        when(commentRepository.findByPostIdAndStatusOrderByCreatedAtAsc("p1", "PUBLISHED")).thenReturn(List.of());
        List<ForumComment> result = forumService.getComments("p1");
        assertNotNull(result);
    }

    @Test
    void upvoteComment_incrementsUpvotes() {
        ForumComment comment = ForumComment.builder().upvotes(1).build();
        comment.setId("c1");
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ForumComment result = forumService.upvoteComment("c1");
        assertEquals(2, result.getUpvotes());
    }

    @Test
    void upvoteComment_throwsNotFound() {
        when(commentRepository.findById("c1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> forumService.upvoteComment("c1"));
    }

    @Test
    void addComment_throwsPostNotFound() {
        when(postRepository.findById("p1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> forumService.addComment("p1", "u1", "user", "content", null));
    }
}
