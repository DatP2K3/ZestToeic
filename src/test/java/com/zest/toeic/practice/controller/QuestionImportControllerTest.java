package com.zest.toeic.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.toeic.shared.config.SecurityConfig;
import com.zest.toeic.shared.scraper.Study4Scraper;
import com.zest.toeic.shared.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = QuestionImportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class QuestionImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Study4Scraper study4Scraper;

    @Test
    @WithMockUser
    void scrapeStudy4_Success() throws Exception {
        Study4Scraper.ScrapingResult mockResult = new Study4Scraper.ScrapingResult();
        mockResult.setImported(10);
        mockResult.setTotalFound(10);

        when(study4Scraper.scrape(anyString(), anyInt())).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/admin/questions/scrape")
                        .param("url", "https://study4.com/test")
                        .param("maxQuestions", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imported").value(10))
                .andExpect(jsonPath("$.data.totalFound").value(10));
    }
}
