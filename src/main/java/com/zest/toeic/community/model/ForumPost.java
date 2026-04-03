package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.ForumPostStatus;
import com.zest.toeic.shared.model.enums.ForumPostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
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
    private ForumPostType type = ForumPostType.DISCUSSION;

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
    private ForumPostStatus status = ForumPostStatus.PUBLISHED;
}
