package com.zest.toeic.monetization.model;

import com.zest.toeic.shared.model.BaseDocument;
import com.zest.toeic.shared.model.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.mongodb.core.index.Indexed;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "payment_transactions")
public class PaymentTransaction extends BaseDocument {

    @Indexed
    private String userId;
    private String subscriptionId;
    private long amount; // in VND
    private String currency;
    private PaymentStatus status;
    private String paymentMethod; // VNPAY
    private String vnpTxnRef;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private String vnpResponseCode;
    private String orderInfo;
}
