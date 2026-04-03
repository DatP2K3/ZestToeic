package com.zest.toeic.community.controller;

import com.zest.toeic.community.model.ForumComment;
import com.zest.toeic.community.model.ForumPost;
import com.zest.toeic.community.service.ForumService;
import com.zest.toeic.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forum")
@Tag(name = "Forum", description = "Discussion Forum & Q&A")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/posts")
    @Operation(summary = "Tạo bài viết mới (Discussion hoặc Q&A)")
    public ResponseEntity<ApiResponse<ForumPost>> createPost(
            Authentication auth, @Valid @RequestBody com.zest.toeic.community.dto.CreateForumPostRequest request) {
        String type = request.type() != null ? request.type() : "DISCUSSION";
        return ResponseEntity.ok(ApiResponse.success(
                forumService.createPost(auth.getName(), auth.getName(), request.title(), request.content(), type, request.tags())));
    }

    @GetMapping("/posts")
    @Operation(summary = "Danh sách bài viết (phân trang, filter)")
    public ResponseEntity<ApiResponse<Page<ForumPost>>> getPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(forumService.getPosts(type, tag, page, size)));
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "Chi tiết bài viết")
    public ResponseEntity<ApiResponse<ForumPost>> getPost(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(forumService.getPost(id)));
    }

    @PostMapping("/posts/{id}/upvote")
    @Operation(summary = "Upvote bài viết")
    public ResponseEntity<ApiResponse<ForumPost>> upvotePost(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(forumService.upvotePost(id)));
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Thêm comment (support threaded reply)")
    public ResponseEntity<ApiResponse<ForumComment>> addComment(
            Authentication auth, @PathVariable String postId, @Valid @RequestBody com.zest.toeic.community.dto.AddForumCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                forumService.addComment(postId, auth.getName(), auth.getName(),
                        request.content(), request.parentId())));
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Danh sách comments của bài viết")
    public ResponseEntity<ApiResponse<List<ForumComment>>> getComments(@PathVariable String postId) {
        return ResponseEntity.ok(ApiResponse.success(forumService.getComments(postId)));
    }

    @PostMapping("/posts/{postId}/best-answer/{commentId}")
    @Operation(summary = "Đánh dấu câu trả lời hay nhất (Q&A only)")
    public ResponseEntity<ApiResponse<ForumPost>> markBestAnswer(
            Authentication auth, @PathVariable String postId, @PathVariable String commentId) {
        return ResponseEntity.ok(ApiResponse.success(
                forumService.markBestAnswer(postId, commentId, auth.getName())));
    }
}
