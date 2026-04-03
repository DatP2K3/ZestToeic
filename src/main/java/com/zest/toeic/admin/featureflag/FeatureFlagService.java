package com.zest.toeic.admin.featureflag;

import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class FeatureFlagService {

    private static final String REDIS_PREFIX = "feature_flag:";
    private static final long CACHE_TTL_MINUTES = 5;

    private final FeatureFlagRepository repository;
    private final StringRedisTemplate redisTemplate;

    public FeatureFlagService(FeatureFlagRepository repository, StringRedisTemplate redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public boolean isEnabled(String flagName, String userId) {
        // Check Redis cache first
        String cacheKey = REDIS_PREFIX + flagName;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        boolean globalEnabled;
        int rollout;

        if (cached != null) {
            String[] parts = cached.split(":");
            globalEnabled = Boolean.parseBoolean(parts[0]);
            rollout = Integer.parseInt(parts[1]);
        } else {
            FeatureFlag flag = repository.findByName(flagName).orElse(null);
            if (flag == null) return false;
            globalEnabled = flag.isEnabled();
            rollout = flag.getRolloutPercentage();
            // Cache in Redis
            redisTemplate.opsForValue().set(cacheKey, globalEnabled + ":" + rollout, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }

        if (!globalEnabled) return false;
        if (rollout >= 100) return true;
        if (rollout <= 0) return false;

        // Deterministic rollout based on userId hash
        int hash = Math.abs((userId + flagName).hashCode() % 100);
        return hash < rollout;
    }

    public List<FeatureFlag> getAll() {
        return repository.findAll();
    }

    public FeatureFlag create(FeatureFlag flag) {
        flag.setId(null);
        return repository.save(flag);
    }

    public FeatureFlag update(String id, FeatureFlag update) {
        FeatureFlag existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found: " + id));
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setEnabled(update.isEnabled());
        existing.setRolloutPercentage(update.getRolloutPercentage());
        invalidateCache(existing.getName());
        return repository.save(existing);
    }

    public void delete(String id) {
        FeatureFlag flag = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found: " + id));
        invalidateCache(flag.getName());
        repository.deleteById(id);
    }

    private void invalidateCache(String flagName) {
        redisTemplate.delete(REDIS_PREFIX + flagName);
    }
}
