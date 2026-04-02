package com.zest.toeic.admin.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String action, String performedBy, String targetType, String targetId,
                    Map<String, Object> details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        repository.save(auditLog);
    }

    public Page<AuditLog> getAllLogs(int page, int size) {
        return repository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    public Page<AuditLog> getLogsByAdmin(String adminId, int page, int size) {
        return repository.findByPerformedByOrderByTimestampDesc(adminId, PageRequest.of(page, size));
    }

    public Page<AuditLog> getLogsByTarget(String targetType, String targetId, int page, int size) {
        return repository.findByTargetTypeAndTargetIdOrderByTimestampDesc(targetType, targetId, PageRequest.of(page, size));
    }
}
