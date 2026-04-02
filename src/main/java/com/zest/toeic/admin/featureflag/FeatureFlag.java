package com.zest.toeic.admin.featureflag;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "feature_flags")
public class FeatureFlag extends BaseDocument {

    @Indexed(unique = true)
    private String name;
    private String description;

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private int rolloutPercentage = 0; // 0-100
}
