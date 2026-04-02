package com.zest.toeic.productivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    @Mock private ReminderService reminderService;
    @InjectMocks private ReminderController controller;
    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("user1", null, List.of());

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void list() throws Exception {
        when(reminderService.getUserReminders("user1")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/reminders").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void create() throws Exception {
        Reminder r = Reminder.builder().type("DAILY_STUDY").schedule("07:30").message("Learn!").build();
        when(reminderService.create(anyString(), any())).thenReturn(r);
        mockMvc.perform(post("/api/v1/reminders")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isCreated());
    }

    @Test
    void update() throws Exception {
        Reminder r = Reminder.builder().type("CUSTOM").schedule("08:00").message("Upd").build();
        when(reminderService.update(eq("r1"), any())).thenReturn(r);
        mockMvc.perform(put("/api/v1/reminders/r1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReminder() throws Exception {
        mockMvc.perform(delete("/api/v1/reminders/r1")).andExpect(status().isOk());
    }
}
