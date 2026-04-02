package com.zest.toeic.admin.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    Page<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy, Pageable pageable);
    Page<AuditLog> findByTargetTypeAndTargetIdOrderByTimestampDesc(String targetType, String targetId, Pageable pageable);
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
