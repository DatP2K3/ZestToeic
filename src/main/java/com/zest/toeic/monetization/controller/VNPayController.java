package com.zest.toeic.monetization.controller;

import com.zest.toeic.monetization.model.PaymentTransaction;
import com.zest.toeic.monetization.service.SubscriptionService;
import com.zest.toeic.monetization.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment", description = "VNPay IPN and Return URL handlers")
public class VNPayController {

    private static final Logger log = LoggerFactory.getLogger(VNPayController.class);

    private final VNPayService vnPayService;
    private final SubscriptionService subscriptionService;

    public VNPayController(VNPayService vnPayService, SubscriptionService subscriptionService) {
        this.vnPayService = vnPayService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN callback (server-to-server)")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN received: {}", params.get("vnp_TxnRef"));
        Map<String, String> response = vnPayService.processIpnCallback(params);

        // If payment successful, activate premium
        if ("00".equals(response.get("RspCode"))) {
            String vnpTxnRef = params.get("vnp_TxnRef");
            PaymentTransaction tx = vnPayService.getTransactionByTxnRef(vnpTxnRef);
            if (tx != null && "SUCCESS".equals(tx.getStatus())) {
                subscriptionService.activatePremium(tx.getUserId(), tx.getId());
                log.info("Premium activated for user {} via VNPay", tx.getUserId());
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay-return")
    @Operation(summary = "VNPay return URL (redirect after payment)")
    public ResponseEntity<Map<String, Object>> vnpayReturn(@RequestParam Map<String, String> params) {
        String responseCode = params.getOrDefault("vnp_ResponseCode", "99");
        String txnRef = params.getOrDefault("vnp_TxnRef", "");

        return ResponseEntity.ok(Map.of(
                "success", "00".equals(responseCode),
                "vnp_TxnRef", txnRef,
                "vnp_ResponseCode", responseCode,
                "message", "00".equals(responseCode) ? "Thanh toan thanh cong" : "Thanh toan that bai"
        ));
    }
}
