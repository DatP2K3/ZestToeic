package com.zest.toeic.monetization.repository;

import com.zest.toeic.monetization.model.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    Optional<PaymentTransaction> findByVnpTxnRef(String vnpTxnRef);
}
