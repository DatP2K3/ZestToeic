package com.zest.toeic.admin.featureflag;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface FeatureFlagRepository extends MongoRepository<FeatureFlag, String> {
    Optional<FeatureFlag> findByName(String name);
}
