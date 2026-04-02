package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "forum_posts")
public class ForumPost extends BaseDocument {

    private String title;
    private String content;

    @Indexed
    private String authorId;
    private String authorName;

    @Builder.Default
    private String type = "DISCUSSION"; // DISCUSSION | QA

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private int upvotes = 0;

    @Builder.Default
    private int downvotes = 0;

    @Builder.Default
    private int commentCount = 0;

    @Builder.Default
    private int viewCount = 0;

    // For QA type
    private String bestAnswerId;

    @Builder.Default
    private boolean resolved = false;

    @Builder.Default
    private String status = "PUBLISHED"; // PUBLISHED, HIDDEN, UNDER_REVIEW, DELETED
}
