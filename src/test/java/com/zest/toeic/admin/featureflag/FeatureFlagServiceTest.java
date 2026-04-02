package com.zest.toeic.admin.featureflag;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock private FeatureFlagRepository repository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @InjectMocks private FeatureFlagService service;

    @BeforeEach
    void setup() { lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps); }

    @Test
    void isEnabled_cached_enabledFull() {
        when(valueOps.get("feature_flag:battle")).thenReturn("true:100");
        assertTrue(service.isEnabled("battle", "user1"));
    }

    @Test
    void isEnabled_cached_disabled() {
        when(valueOps.get("feature_flag:battle")).thenReturn("false:100");
        assertFalse(service.isEnabled("battle", "user1"));
    }

    @Test
    void isEnabled_cached_zeroRollout() {
        when(valueOps.get("feature_flag:battle")).thenReturn("true:0");
        assertFalse(service.isEnabled("battle", "user1"));
    }

    @Test
    void isEnabled_notCached_fromDb() {
        when(valueOps.get("feature_flag:test")).thenReturn(null);
        FeatureFlag flag = FeatureFlag.builder().name("test").enabled(true).rolloutPercentage(100).build();
        when(repository.findByName("test")).thenReturn(Optional.of(flag));

        assertTrue(service.isEnabled("test", "user1"));
        verify(valueOps).set(eq("feature_flag:test"), eq("true:100"), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void isEnabled_notFoundInDb() {
        when(valueOps.get("feature_flag:x")).thenReturn(null);
        when(repository.findByName("x")).thenReturn(Optional.empty());
        assertFalse(service.isEnabled("x", "user1"));
    }

    @Test
    void isEnabled_partialRollout() {
        when(valueOps.get("feature_flag:feat")).thenReturn("true:50");
        // Deterministic — just verify it doesn't throw
        service.isEnabled("feat", "user1");
        service.isEnabled("feat", "user2");
    }

    @Test
    void getAll() {
        when(repository.findAll()).thenReturn(List.of(FeatureFlag.builder().name("f1").build()));
        assertEquals(1, service.getAll().size());
    }

    @Test
    void create() {
        FeatureFlag flag = FeatureFlag.builder().name("new").build();
        when(repository.save(any())).thenReturn(flag);
        assertNotNull(service.create(flag));
    }

    @Test
    void update_success() {
        FeatureFlag existing = FeatureFlag.builder().name("old").build();
        existing.setId("id1");
        when(repository.findById("id1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        FeatureFlag update = FeatureFlag.builder().name("new").description("desc").enabled(true).rolloutPercentage(50).build();
        FeatureFlag result = service.update("id1", update);
        assertNotNull(result);
        verify(redisTemplate).delete("feature_flag:new");
    }

    @Test
    void update_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update("x", new FeatureFlag()));
    }

    @Test
    void delete_success() {
        FeatureFlag flag = FeatureFlag.builder().name("del").build();
        flag.setId("id1");
        when(repository.findById("id1")).thenReturn(Optional.of(flag));
        service.delete("id1");
        verify(repository).deleteById("id1");
        verify(redisTemplate).delete("feature_flag:del");
    }

    @Test
    void delete_notFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete("x"));
    }
}
