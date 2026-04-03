package com.zest.toeic.community.dto;

import jakarta.validation.constraints.NotBlank;

public record AddForumCommentRequest(
        @NotBlank(message = "Content is required") String content,
        String parentId
) {}
