package com.zest.toeic.admin.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository repository;
    @InjectMocks private AuditService service;

    @Test
    void log_success() {
        when(repository.save(any())).thenReturn(AuditLog.builder().id("1").build());
        service.log("USER_BANNED", "admin1", "USER", "u1", Map.of("reason", "spam"), "127.0.0.1");
        verify(repository).save(any(AuditLog.class));
    }

    @Test
    void getAllLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(AuditLog.builder().id("1").action("TEST").build()));
        when(repository.findAllByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(page);
        assertEquals(1, service.getAllLogs(0, 20).getContent().size());
    }

    @Test
    void getLogsByAdmin() {
        Page<AuditLog> page = new PageImpl<>(List.of(AuditLog.builder().id("1").build()));
        when(repository.findByPerformedByOrderByTimestampDesc(eq("admin1"), any())).thenReturn(page);
        assertEquals(1, service.getLogsByAdmin("admin1", 0, 20).getContent().size());
    }

    @Test
    void getLogsByTarget() {
        Page<AuditLog> page = new PageImpl<>(List.of(AuditLog.builder().id("1").build()));
        when(repository.findByTargetTypeAndTargetIdOrderByTimestampDesc(eq("USER"), eq("u1"), any())).thenReturn(page);
        assertEquals(1, service.getLogsByTarget("USER", "u1", 0, 20).getContent().size());
    }
}
