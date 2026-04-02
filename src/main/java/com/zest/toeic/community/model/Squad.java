package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "squads")
public class Squad extends BaseDocument {

    private String name;
    private String ownerId;

    @Builder.Default
    private List<SquadMember> members = new ArrayList<>();

    @Builder.Default
    private int streak = 0;

    private LocalDate lastStreakCheckDate;

    @Builder.Default
    private int maxMembers = 5;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SquadMember {
        private String userId;
        private String displayName;
        private Instant joinedAt;
    }
}
