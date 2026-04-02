package com.zest.toeic.admin.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock private AuditService auditService;
    @InjectMocks private AuditController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(conv).build();
    }

    @Test
    void getAllLogs() throws Exception {
        Page<AuditLog> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        when(auditService.getAllLogs(anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/audit-logs")).andExpect(status().isOk());
    }

    @Test
    void getByAdmin() throws Exception {
        Page<AuditLog> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        when(auditService.getLogsByAdmin(anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/audit-logs/admin/admin1")).andExpect(status().isOk());
    }

    @Test
    void getByTarget() throws Exception {
        Page<AuditLog> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        when(auditService.getLogsByTarget(anyString(), anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/audit-logs/target/USER/u1")).andExpect(status().isOk());
    }
}
