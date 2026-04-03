package com.zest.toeic.community.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateForumPostRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Content is required") String content,
        String type,
        List<String> tags
) {}
