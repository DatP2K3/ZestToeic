package com.zest.toeic;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disabled for pure unit-testing coverage. Runs on CI with proper DBs")
class ZestToeicApplicationTests {

    @Test
    void contextLoads() {
    }
}
