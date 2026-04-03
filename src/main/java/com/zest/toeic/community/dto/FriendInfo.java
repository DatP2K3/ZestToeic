package com.zest.toeic.community.dto;

import com.zest.toeic.shared.model.enums.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendInfo {
    private String friendId;
    private String displayName;
    private int level;
    private long totalXp;
    private int streakCurrent;
    private FriendStatus status;
}
