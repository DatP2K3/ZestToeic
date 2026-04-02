package com.zest.toeic.admin.audit;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    private String action;        // USER_BANNED, QUESTION_DELETED, FLAG_UPDATED
    private String performedBy;   // admin userId
    private String targetType;    // USER, QUESTION, FEATURE_FLAG
    private String targetId;
    private Map<String, Object> details;
    private String ipAddress;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
