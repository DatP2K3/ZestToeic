package com.zest.toeic.gamification.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_badges")
@CompoundIndex(name = "idx_user_badge", def = "{'userId': 1, 'badgeId': 1}", unique = true)
public class UserBadge extends BaseDocument {

    private String userId;
    private String badgeId;
    private String badgeName;
    private LocalDateTime earnedAt;
}
