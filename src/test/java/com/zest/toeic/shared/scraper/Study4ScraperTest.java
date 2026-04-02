package com.zest.toeic.shared.scraper;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Study4ScraperTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionValidator questionValidator;

    @InjectMocks
    private Study4Scraper study4Scraper;

    private MockedStatic<Jsoup> jsoupMockedStatic;

    @BeforeEach
    void setUp() {
        jsoupMockedStatic = mockStatic(Jsoup.class);
        // Allow Jsoup.parse to work normally so we can construct test Documents
        jsoupMockedStatic.when(() -> Jsoup.parse(anyString())).thenCallRealMethod();
    }

    @AfterEach
    void tearDown() {
        jsoupMockedStatic.close();
    }

    @Test
    void scrape_ConnectionError_ReturnsError() throws IOException {
        Connection connectionMock = mock(Connection.class);
        jsoupMockedStatic.when(() -> Jsoup.connect(anyString())).thenReturn(connectionMock);
        when(connectionMock.userAgent(anyString())).thenReturn(connectionMock);
        when(connectionMock.timeout(anyInt())).thenReturn(connectionMock);
        when(connectionMock.get()).thenThrow(new IOException("Timeout"));

        Study4Scraper.ScrapingResult result = study4Scraper.scrape("http://test.com", 10);

        assertEquals(0, result.getImported());
        assertEquals("Connection failed: Timeout", result.getError());
    }

    @Test
    void scrape_Success_ParsesAndSaves() throws IOException {
        Connection connectionMock = mock(Connection.class);
        jsoupMockedStatic.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
        when(connectionMock.userAgent(anyString())).thenReturn(connectionMock);
        when(connectionMock.timeout(anyInt())).thenReturn(connectionMock);

        String html = """
                <html>
                    <body>
                        <div class="question-block" data-part="5">
                            <p class="question-content">This is question 1.</p>
                            <ul>
                                <li class="option">A. Option 1</li>
                                <li class="option">B. Option 2</li>
                                <li class="option">C. Option 3</li>
                                <li class="option">D. Option 4</li>
                            </ul>
                            <div class="correct-answer" data-answer="A"></div>
                            <div class="explanation">Explanation here</div>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        when(connectionMock.get()).thenReturn(doc);

        when(questionValidator.isValid(any(Question.class))).thenReturn(true);
        when(questionValidator.isDuplicate(any(Question.class), eq(questionRepository))).thenReturn(false);

        Study4Scraper.ScrapingResult result = study4Scraper.scrape("http://test.com", 10);

        assertEquals(1, result.getTotalFound());
        assertEquals(1, result.getImported());
        assertEquals(0, result.getErrors());
        assertEquals(0, result.getInvalid());
        assertEquals(0, result.getDuplicates());

        verify(questionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void scrape_IncompleteBlock_FailsValidation() throws IOException {
        Connection connectionMock = mock(Connection.class);
        jsoupMockedStatic.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
        when(connectionMock.userAgent(anyString())).thenReturn(connectionMock);
        when(connectionMock.timeout(anyInt())).thenReturn(connectionMock);

        String html = """
                <html>
                    <body>
                        <div class="question-block" data-part="5">
                            <p class="question-content">Only question, no options</p>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        when(connectionMock.get()).thenReturn(doc);

        // We don't even need to mock validator if parsing fails and returns null early
        Study4Scraper.ScrapingResult result = study4Scraper.scrape("http://test.com", 10);

        assertEquals(1, result.getTotalFound());
        assertEquals(0, result.getImported());
        assertEquals(1, result.getInvalid());
        
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    void scrape_DuplicateQuestion_SkipsSaving() throws IOException {
        Connection connectionMock = mock(Connection.class);
        jsoupMockedStatic.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
        when(connectionMock.userAgent(anyString())).thenReturn(connectionMock);
        when(connectionMock.timeout(anyInt())).thenReturn(connectionMock);

        String html = """
                <html>
                    <body>
                        <div class="question-block" data-part="5">
                            <p class="question-content">This is a duplicate question.</p>
                            <ul>
                                <li class="option">A. Option 1</li>
                                <li class="option">B. Option 2</li>
                                <li class="option">C. Option 3</li>
                                <li class="option">D. Option 4</li>
                            </ul>
                            <div class="correct-answer" data-answer="B"></div>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        when(connectionMock.get()).thenReturn(doc);

        when(questionValidator.isValid(any(Question.class))).thenReturn(true);
        when(questionValidator.isDuplicate(any(Question.class), eq(questionRepository))).thenReturn(true);

        Study4Scraper.ScrapingResult result = study4Scraper.scrape("http://test.com", 10);

        assertEquals(1, result.getTotalFound());
        assertEquals(0, result.getImported());
        assertEquals(1, result.getDuplicates());

        verify(questionRepository, never()).saveAll(anyList());
    }
}
