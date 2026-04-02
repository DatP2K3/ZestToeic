package com.zest.toeic.admin.featureflag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagControllerTest {

    @Mock private FeatureFlagService service;
    @InjectMocks private FeatureFlagController controller;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", null, List.of()));
    }

    @Test
    void listAll() throws Exception {
        when(service.getAll()).thenReturn(List.of(FeatureFlag.builder().name("f1").build()));
        mockMvc.perform(get("/api/v1/admin/feature-flags"))
                .andExpect(status().isOk());
    }

    @Test
    void create() throws Exception {
        FeatureFlag flag = FeatureFlag.builder().name("new").enabled(true).build();
        when(service.create(any())).thenReturn(flag);
        mockMvc.perform(post("/api/v1/admin/feature-flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(flag)))
                .andExpect(status().isCreated());
    }

    @Test
    void update() throws Exception {
        FeatureFlag flag = FeatureFlag.builder().name("upd").build();
        when(service.update(eq("id1"), any())).thenReturn(flag);
        mockMvc.perform(put("/api/v1/admin/feature-flags/id1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(flag)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFlag() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/feature-flags/id1"))
                .andExpect(status().isOk());
    }

    @Test
    void checkFeature() throws Exception {
        when(service.isEnabled(eq("battle"), anyString())).thenReturn(true);
        mockMvc.perform(get("/api/v1/features/battle/check")
                        .principal(new UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk());
    }
}
