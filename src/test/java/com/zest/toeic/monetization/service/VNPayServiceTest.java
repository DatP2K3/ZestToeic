package com.zest.toeic.monetization.service;

import com.zest.toeic.monetization.model.PaymentTransaction;
import com.zest.toeic.monetization.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        vnPayService = new VNPayService(transactionRepository);
        ReflectionTestUtils.setField(vnPayService, "vnpTmnCode", "TESTCODE");
        ReflectionTestUtils.setField(vnPayService, "vnpHashSecret", "TESTSECRET1234567890OIQWEBNMZXCV");
        ReflectionTestUtils.setField(vnPayService, "vnpPayUrl", "http://sandbox.vnpayment.vn");
        ReflectionTestUtils.setField(vnPayService, "vnpReturnUrl", "http://localhost/return");
    }

    @Test
    void createPaymentUrl_shouldReturnValidUrlAndSaveTransaction() {
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        String url = vnPayService.createPaymentUrl("u1", 99000, "Upgrade", "127.0.0.1");

        assertNotNull(url);
        assertTrue(url.startsWith("http://sandbox.vnpayment.vn?"));
        assertTrue(url.contains("vnp_Amount=9900000")); // Amount in VND * 100
        assertTrue(url.contains("vnp_OrderInfo=Upgrade"));
        
        verify(transactionRepository).save(any(PaymentTransaction.class));
    }

    @Test
    void processIpnCallback_invalidChecksum_returns97() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "testRef");
        params.put("vnp_Amount", "9900000");
        params.put("vnp_SecureHash", "invalidhash");

        Map<String, String> response = vnPayService.processIpnCallback(params);

        assertEquals("97", response.get("RspCode"));
    }

    @Test
    void processIpnCallback_validChecksum_orderNotFound_returns01() {
        Map<String, String> params = generateValidParamsAndHash("testRef", "9900000", "00");

        when(transactionRepository.findByVnpTxnRef("testRef")).thenReturn(Optional.empty());

        Map<String, String> response = vnPayService.processIpnCallback(params);
        assertEquals("01", response.get("RspCode"));
    }

    @Test
    void processIpnCallback_validChecksum_invalidAmount_returns04() {
        Map<String, String> params = generateValidParamsAndHash("testRef", "9900000", "00");

        PaymentTransaction tx = PaymentTransaction.builder().vnpTxnRef("testRef").amount(50000).build();
        when(transactionRepository.findByVnpTxnRef("testRef")).thenReturn(Optional.of(tx));

        Map<String, String> response = vnPayService.processIpnCallback(params);
        assertEquals("04", response.get("RspCode"));
    }

    @Test
    void processIpnCallback_validChecksum_alreadyConfirmed_returns02() {
        Map<String, String> params = generateValidParamsAndHash("testRef", "9900000", "00");

        PaymentTransaction tx = PaymentTransaction.builder().vnpTxnRef("testRef").amount(99000).status("SUCCESS").build();
        when(transactionRepository.findByVnpTxnRef("testRef")).thenReturn(Optional.of(tx));

        Map<String, String> response = vnPayService.processIpnCallback(params);
        assertEquals("02", response.get("RspCode"));
    }

    @Test
    void processIpnCallback_success_updatesStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "testRef");
        params.put("vnp_Amount", "9900000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "bank123");
        params.put("vnp_BankCode", "NCB");
        
        params = generateValidParamsAndHash(params);

        PaymentTransaction tx = PaymentTransaction.builder().vnpTxnRef("testRef").amount(99000).status("PENDING").build();
        when(transactionRepository.findByVnpTxnRef("testRef")).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, String> response = vnPayService.processIpnCallback(params);
        
        assertEquals("00", response.get("RspCode"));
        assertEquals("SUCCESS", tx.getStatus());
        assertEquals("bank123", tx.getVnpTransactionNo());
        assertEquals("NCB", tx.getVnpBankCode());
        verify(transactionRepository).save(tx);
    }

    private Map<String, String> generateValidParamsAndHash(String txnRef, String amount, String responseCode) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Amount", amount);
        params.put("vnp_ResponseCode", responseCode);
        return generateValidParamsAndHash(params);
    }

    private Map<String, String> generateValidParamsAndHash(Map<String, String> params) {
        try {
            java.lang.reflect.Method method = VNPayService.class.getDeclaredMethod("hmacSHA512", String.class, String.class);
            method.setAccessible(true);
            
            // Remove hash if exists to calculate properly
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            Map<String, String> queryFields = new java.util.TreeMap<>(params);
            StringBuilder hashData = new StringBuilder();
            java.util.Iterator<Map.Entry<String, String>> itr = queryFields.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    hashData.append(entry.getKey()).append('=').append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                    if (itr.hasNext()) hashData.append('&');
                }
            }
            // Remove trailing & if any
            if (hashData.length() > 0 && hashData.charAt(hashData.length() - 1) == '&') {
                hashData.deleteCharAt(hashData.length() - 1);
            }
            
            String validHash = (String) method.invoke(vnPayService, "TESTSECRET1234567890OIQWEBNMZXCV", hashData.toString());
            params.put("vnp_SecureHash", validHash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test hash", e);
        }
        return params;
    }
}
