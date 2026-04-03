package com.zest.toeic.monetization.service;

import com.zest.toeic.monetization.model.PaymentTransaction;
import com.zest.toeic.monetization.repository.PaymentTransactionRepository;
import com.zest.toeic.shared.model.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class VNPayService {

    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);

    @Value("${vnpay.tmn-code:VNPAY_TMN}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:VNPAY_SECRET}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:8080/api/v1/payment/vnpay-return}")
    private String vnpReturnUrl;

    private final PaymentTransactionRepository transactionRepository;

    public VNPayService(PaymentTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public String createPaymentUrl(String userId, long amountVND, String orderInfo, String ipAddr) {
        String vnpTxnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        long vnpAmount = amountVND * 100; // VNPAY requires amount * 100

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddr != null ? ipAddr : "127.0.0.1");

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        vnpParams.put("vnp_CreateDate", formatter.format(cal.getTime()));

        cal.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(cal.getTime()));

        // Build hash data and query
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = vnpParams.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        String paymentUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;

        // Save pending transaction
        PaymentTransaction tx = PaymentTransaction.builder()
                .userId(userId)
                .amount(amountVND)
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .paymentMethod("VNPAY")
                .vnpTxnRef(vnpTxnRef)
                .orderInfo(orderInfo)
                .build();
        transactionRepository.save(tx);

        log.info("Created VNPAY payment URL for user {} with txnRef {}", userId, vnpTxnRef);
        return paymentUrl;
    }

    public Map<String, String> processIpnCallback(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        String vnpSecureHash = params.get("vnp_SecureHash");
        Map<String, String> fields = new TreeMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = fields.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
            hashData.append('=');
            hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            if (itr.hasNext()) hashData.append('&');
        }

        String signValue = hmacSHA512(vnpHashSecret, hashData.toString());

        if (!signValue.equals(vnpSecureHash)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return response;
        }

        String vnpTxnRef = params.get("vnp_TxnRef");
        Optional<PaymentTransaction> optTx = transactionRepository.findByVnpTxnRef(vnpTxnRef);

        if (optTx.isEmpty()) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }

        PaymentTransaction tx = optTx.get();

        long vnpAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100;
        if (tx.getAmount() != vnpAmount) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid amount");
            return response;
        }

        if (PaymentStatus.PENDING != tx.getStatus()) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        String responseCode = params.getOrDefault("vnp_ResponseCode", "99");
        tx.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        tx.setVnpBankCode(params.get("vnp_BankCode"));
        tx.setVnpResponseCode(responseCode);

        if ("00".equals(responseCode)) {
            tx.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment SUCCESS for txnRef {}", vnpTxnRef);
        } else {
            tx.setStatus(PaymentStatus.FAILED);
            log.warn("Payment FAILED for txnRef {}, responseCode: {}", vnpTxnRef, responseCode);
        }

        transactionRepository.save(tx);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }

    public PaymentTransaction getTransactionByTxnRef(String vnpTxnRef) {
        return transactionRepository.findByVnpTxnRef(vnpTxnRef).orElse(null);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC SHA512", e);
        }
    }
}
