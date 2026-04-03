package com.zest.toeic.productivity.controller;
import com.zest.toeic.productivity.model.StudyPlan;
import com.zest.toeic.productivity.service.StudyPlanService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.shared.model.enums.GoalStatus;
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
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudyPlanControllerTest {

    @Mock private StudyPlanService studyPlanService;
    @InjectMocks private StudyPlanController controller;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("user1", null, List.of());

    @BeforeEach
    void setup() { mockMvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void getCurrentPlan_exists() throws Exception {
        StudyPlan plan = StudyPlan.builder().userId("user1").status(GoalStatus.ACTIVE).build();
        when(studyPlanService.getCurrentPlan("user1")).thenReturn(Optional.of(plan));
        mockMvc.perform(get("/api/v1/planner").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void getCurrentPlan_empty() throws Exception {
        when(studyPlanService.getCurrentPlan("user1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/planner").principal(auth)).andExpect(status().isOk());
    }

    @Test
    void generatePlan() throws Exception {
        StudyPlan plan = StudyPlan.builder().userId("user1").build();
        when(studyPlanService.generatePlan(anyString(), anyDouble(), any())).thenReturn(plan);
        Map<String, Object> body = Map.of("weeklyHours", 8.0, "focusAreas", List.of("Part 5"));
        mockMvc.perform(post("/api/v1/planner/generate")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void completeTask() throws Exception {
        StudyPlan plan = StudyPlan.builder().build();
        when(studyPlanService.completeTask("p1", 1, 0)).thenReturn(plan);
        mockMvc.perform(put("/api/v1/planner/p1/tasks/1/0/complete")).andExpect(status().isOk());
    }

    @Test
    void adjustPlan() throws Exception {
        StudyPlan plan = StudyPlan.builder().build();
        when(studyPlanService.adjustPlan("p1")).thenReturn(plan);
        mockMvc.perform(post("/api/v1/planner/p1/adjust")).andExpect(status().isOk());
    }
}
