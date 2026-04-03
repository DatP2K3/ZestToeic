package com.zest.toeic.monetization.controller;

import com.zest.toeic.monetization.model.PaymentTransaction;
import com.zest.toeic.monetization.service.SubscriptionService;
import com.zest.toeic.monetization.service.VNPayService;
import com.zest.toeic.shared.model.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VNPayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VNPayService vnPayService;
    
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private VNPayController vnPayController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vnPayController)
                .setControllerAdvice(new com.zest.toeic.shared.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void vnpayIpn_returnsOk() throws Exception {
        when(vnPayService.processIpnCallback(any())).thenReturn(Map.of("RspCode", "00"));
        PaymentTransaction tx = PaymentTransaction.builder().status(PaymentStatus.SUCCESS).userId("u1").build();
        tx.setId("tx1");
        when(vnPayService.getTransactionByTxnRef(anyString())).thenReturn(tx);

        mockMvc.perform(get("/api/v1/payment/vnpay-ipn")
                .param("vnp_Amount", "100000")
                .param("vnp_TxnRef", "testRef"))
                .andExpect(status().isOk());
    }

    @Test
    void vnpayReturn_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/payment/vnpay-return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "testRef"))
                .andExpect(status().isOk());
    }
}
