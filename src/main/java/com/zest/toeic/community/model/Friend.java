package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "friends")
@CompoundIndex(name = "idx_sender_receiver", def = "{'senderId': 1, 'receiverId': 1}", unique = true)
public class Friend extends BaseDocument {

    private String senderId;
    private String receiverId;

    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;
}

