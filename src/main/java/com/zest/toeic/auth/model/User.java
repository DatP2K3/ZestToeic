package com.zest.toeic.auth.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class User extends BaseDocument {

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private String displayName;
    private String country;

    @Builder.Default
    private int level = 1;

    @Builder.Default
    private long totalXp = 0;

    @Builder.Default
    private String subscriptionTier = "FREE";

    @Builder.Default
    private String role = "USER";

    @Builder.Default
    private String status = "ACTIVE";

    @Builder.Default
    private int streakCurrent = 0;

    @Builder.Default
    private int streakLongest = 0;

    private Integer placementScore;
}
