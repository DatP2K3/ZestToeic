package com.zest.toeic.notification;

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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @Mock private NotificationPreferenceService preferenceService;
    @InjectMocks private NotificationController controller;
    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("user1", null, List.of());

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(conv).build();
    }

    @Test
    void list() throws Exception {
        Page<Notification> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        when(notificationService.getNotifications(anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/notifications").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void unreadCount() throws Exception {
        when(notificationService.getUnreadCount(anyString())).thenReturn(3L);
        mockMvc.perform(get("/api/v1/notifications/unread-count").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void markAsRead() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/n1/read")).andExpect(status().isOk());
    }

    @Test
    void markAllAsRead() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/read-all").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void getPreferences() throws Exception {
        NotificationPreference pref = NotificationPreference.builder().userId("user1").build();
        when(preferenceService.getPreference(anyString())).thenReturn(pref);
        mockMvc.perform(get("/api/v1/notifications/preferences").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void updatePreferences() throws Exception {
        NotificationPreference pref = NotificationPreference.builder().userId("user1").build();
        when(preferenceService.updatePreference(anyString(), any())).thenReturn(pref);
        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pref)))
                .andExpect(status().isOk());
    }
}
