package com.zest.toeic.admin.audit;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "target_idx", def = "{'targetType': 1, 'targetId': 1}")
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    private String action;        // USER_BANNED, QUESTION_DELETED, FLAG_UPDATED
    @Indexed
    private String performedBy;   // admin userId
    private String targetType;    // USER, QUESTION, FEATURE_FLAG
    private String targetId;
    private Map<String, Object> details;
    private String ipAddress;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
