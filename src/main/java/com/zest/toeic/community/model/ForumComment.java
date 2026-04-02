package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "forum_comments")
public class ForumComment extends BaseDocument {

    @Indexed
    private String postId;

    private String parentId; // null = top-level, non-null = reply to another comment

    private String content;
    private String authorId;
    private String authorName;

    @Builder.Default
    private int upvotes = 0;

    @Builder.Default
    private int downvotes = 0;

    @Builder.Default
    private boolean isBestAnswer = false;

    @Builder.Default
    private String status = "PUBLISHED"; // PUBLISHED, HIDDEN, DELETED
}
