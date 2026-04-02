package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "moderation_actions")
public class ModerationAction extends BaseDocument {

    private String targetId;       // postId or commentId
    private String targetType;     // POST, COMMENT
    private String action;         // HIDE, DELETE, WARN, STRIKE
    private String reason;
    private String moderatorId;    // "SYSTEM" for auto-moderation
    private String detectionMethod; // REGEX, AI, MANUAL
    private double toxicityScore;  // 0.0 - 1.0 from AI

    @Builder.Default
    private LocalDateTime actionDate = LocalDateTime.now();
}
