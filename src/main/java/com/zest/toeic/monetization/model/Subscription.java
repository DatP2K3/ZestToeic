package com.zest.toeic.monetization.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "subscriptions")
public class Subscription extends BaseDocument {

    @Indexed
    private String userId;

    @Builder.Default
    private String plan = "FREE"; // FREE, PREMIUM

    private Instant startDate;
    private Instant endDate;

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, CANCELLED, EXPIRED

    @Builder.Default
    private boolean autoRenew = true;

    private String paymentMethod; // VNPAY, MOCK
    private String lastTransactionId;
}
