package com.zest.toeic.shared.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiKeyManagerTest {

    private GeminiKeyManager keyManager;

    @BeforeEach
    void setUp() {
        keyManager = new GeminiKeyManager();
        ReflectionTestUtils.setField(keyManager, "failureThreshold", 2);
        ReflectionTestUtils.setField(keyManager, "halfOpenAfterSeconds", 1); // 1 second for fast testing
    }

    @Test
    void init_NoKeys_FallbackKeyAlsoEmpty() {
        ReflectionTestUtils.setField(keyManager, "configKeys", List.of());
        ReflectionTestUtils.setField(keyManager, "fallbackKey", "");
        keyManager.init();

        assertFalse(keyManager.hasAvailableKeys());
        assertThrows(IllegalStateException.class, () -> keyManager.getNextAvailableKey());
    }

    @Test
    void init_WithFallbackKey() {
        ReflectionTestUtils.setField(keyManager, "configKeys", List.of());
        ReflectionTestUtils.setField(keyManager, "fallbackKey", "fallback-key");
        keyManager.init();

        assertTrue(keyManager.hasAvailableKeys());
        assertEquals("fallback-key", keyManager.getNextAvailableKey());
        assertEquals("fallback-key", keyManager.getNextAvailableKey()); // round robin with 1 key
    }

    @Test
    void init_WithConfigKeys() {
        ReflectionTestUtils.setField(keyManager, "configKeys", List.of("key1", "key2", "  key3  "));
        keyManager.init();

        assertTrue(keyManager.hasAvailableKeys());
        assertEquals("key1", keyManager.getNextAvailableKey());
        assertEquals("key2", keyManager.getNextAvailableKey());
        assertEquals("key3", keyManager.getNextAvailableKey()); // trimmed
        assertEquals("key1", keyManager.getNextAvailableKey()); // round robin rollover
    }

    @Test
    void circuitBreaker_OpensAfterThresholdAndHalfOpensAfterTTL() throws InterruptedException {
        ReflectionTestUtils.setField(keyManager, "configKeys", List.of("keyA", "keyB"));
        keyManager.init();

        // Failure threshold is 2
        keyManager.recordFailure("keyA"); // count=1
        keyManager.recordFailure("keyA"); // count=2 => OPEN

        // Now keyA is NOT available, getNextAvailableKey should only return keyB
        assertEquals("keyB", keyManager.getNextAvailableKey());
        assertEquals("keyB", keyManager.getNextAvailableKey());

        keyManager.recordFailure("keyB");
        keyManager.recordFailure("keyB"); // keyB also OPEN

        assertFalse(keyManager.hasAvailableKeys());
        assertThrows(IllegalStateException.class, () -> keyManager.getNextAvailableKey());

        // Wait for 1.1s for half open
        Thread.sleep(1100);

        assertTrue(keyManager.hasAvailableKeys());
        assertEquals("keyA", keyManager.getNextAvailableKey()); // Returns keyA again since it's half open
        assertEquals("keyB", keyManager.getNextAvailableKey());
    }

    @Test
    void recordSuccess_ResetsFailures() {
        ReflectionTestUtils.setField(keyManager, "configKeys", List.of("keyX"));
        keyManager.init();

        keyManager.recordFailure("keyX"); // count=1
        keyManager.recordSuccess("keyX"); // count=0

        keyManager.recordFailure("keyX"); // count=1
        assertTrue(keyManager.hasAvailableKeys()); // Threshold is 2, so it shouldn't open
    }
}
