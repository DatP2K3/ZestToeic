package com.zest.toeic.shared.ai;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GeminiKeyManager {

    private static final Logger log = LoggerFactory.getLogger(GeminiKeyManager.class);

    @Value("${ai.gemini.keys:}")
    private List<String> configKeys;

    @Value("${ai.gemini.api-key:}")
    private String fallbackKey;

    @Value("${ai.gemini.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${ai.gemini.circuit-breaker.half-open-after-seconds:60}")
    private int halfOpenAfterSeconds;

    private final List<KeyState> keys = new ArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final ConcurrentHashMap<String, KeyState> keyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (configKeys != null && !configKeys.isEmpty() && !(configKeys.size() == 1 && configKeys.get(0).isBlank())) {
            for (String key : configKeys) {
                String cleanKey = key.trim();
                if (!cleanKey.isEmpty()) {
                    KeyState ks = new KeyState(cleanKey);
                    keys.add(ks);
                    keyMap.put(cleanKey, ks);
                }
            }
        } else if (fallbackKey != null && !fallbackKey.isBlank()) {
            KeyState ks = new KeyState(fallbackKey.trim());
            keys.add(ks);
            keyMap.put(fallbackKey.trim(), ks);
        }
        log.info("Initialized GeminiKeyManager with {} keys", keys.size());
    }

    public boolean hasAvailableKeys() {
        if (keys.isEmpty()) return false;
        for (KeyState key : keys) {
            if (key.isAvailable(halfOpenAfterSeconds)) return true;
        }
        return false;
    }

    public String getNextAvailableKey() {
        if (keys.isEmpty()) {
            throw new IllegalStateException("Gemini API keys not configured");
        }

        int startIdx = currentIndex.get();
        for (int i = 0; i < keys.size(); i++) {
            int idx = (Math.abs(startIdx + i)) % keys.size();
            KeyState key = keys.get(idx);
            if (key.isAvailable(halfOpenAfterSeconds)) {
                // Move index for next time (Round Robin)
                currentIndex.compareAndSet(startIdx, (idx + 1) % keys.size());
                return key.getKey();
            }
        }

        throw new IllegalStateException("All Gemini API keys are currently exhausted (Circuit breaker open)");
    }

    public void recordSuccess(String keyStr) {
        KeyState ks = keyMap.get(keyStr);
        if (ks != null) {
            ks.recordSuccess();
        }
    }

    public void recordFailure(String keyStr) {
        KeyState ks = keyMap.get(keyStr);
        if (ks != null) {
            ks.recordFailure(failureThreshold);
        }
    }

    // visible for testing
    protected List<KeyState> getKeys() {
        return keys;
    }

    protected static class KeyState {
        private final String key;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private volatile Instant circuitOpenedAt;

        public KeyState(String key) {
            this.key = key;
        }

        public String getKey() { return key; }

        public boolean isAvailable(int halfOpenSeconds) {
            if (circuitOpenedAt == null) return true;
            // Half open after N seconds
            if (Instant.now().isAfter(circuitOpenedAt.plusSeconds(halfOpenSeconds))) {
                return true;
            }
            return false;
        }

        public void recordSuccess() {
            failureCount.set(0);
            circuitOpenedAt = null;
        }

        public void recordFailure(int threshold) {
            int currentFailures = failureCount.incrementAndGet();
            if (currentFailures >= threshold) {
                if (circuitOpenedAt == null) {
                    circuitOpenedAt = Instant.now();
                    log.warn("Circuit Breaker OPEN for Gemini Key ends with ...{}. Retry after seconds.",
                            key.length() > 4 ? key.substring(key.length() - 4) : key);
                }
            }
        }
        
        public int getFailureCount() { return failureCount.get(); }
    }
}
