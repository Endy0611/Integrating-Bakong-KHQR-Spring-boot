package com.example.integration_with_bakong_khqr.service.impl;

import com.example.integration_with_bakong_khqr.config.BakongProperties;
import com.example.integration_with_bakong_khqr.constraint.OrderStatus;
import com.example.integration_with_bakong_khqr.constraint.PaymentMethod;
import com.example.integration_with_bakong_khqr.model.entity.Order;
import com.example.integration_with_bakong_khqr.model.response.BakongPaymentResponse;
import com.example.integration_with_bakong_khqr.repository.OrderRepository;
import com.example.integration_with_bakong_khqr.service.OrderService;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BakongProperties bakongProperties;
    private final RestTemplate bakongRestTemplate;

    @Override
    @Transactional
    public Order generateQRCode() { //   no id param
        // Always create a fresh order
        Order order = new Order();
        order.setAmount(new BigDecimal("0.10"));
        order.setCurrency("USD");
        order.setPaymentMethod(PaymentMethod.KHQR);
        order.setStatus(OrderStatus.PENDING);
        order.setPaid(false);
        order = orderRepository.save(order);

        long expirationTimestamp = System.currentTimeMillis() + (60 * 1000);
        IndividualInfo info = new IndividualInfo();
        info.setBakongAccountId(bakongProperties.getAccountId());
        info.setMerchantName(bakongProperties.getMerchantName());
        info.setMerchantCity(bakongProperties.getMerchantCity());
        info.setAmount(order.getAmount().doubleValue());
        info.setCurrency(KHQRCurrency.USD);
        info.setExpirationTimestamp(expirationTimestamp);

        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(info);

        if (response != null && response.getData() != null) {
            KHQRData data = response.getData();
            order.setQrCode(data.getQr());
            order.setQrMd5(data.getMd5());
            order.setQrExpiration(expirationTimestamp);
            return orderRepository.save(order);
        } else {
            if (response != null && response.getKHQRStatus() != null) {
                log.error("Bakong Error {}: {}",
                        response.getKHQRStatus().getCode(),
                        response.getKHQRStatus().getMessage());
            }
            throw new RuntimeException("Failed to generate QR code.");
        }
    }

    @Override
    @Transactional
    public Order checkPayment(Long id, String qrMd5) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (Boolean.TRUE.equals(order.getPaid())) {
            return order; // already paid, return immediately
        }

        if (order.getQrExpiration() != null &&
                System.currentTimeMillis() > order.getQrExpiration()) {
            return order; // expired, just return — don't throw
        }

        String url = bakongProperties.getBaseUrl() + "/v1/check_transaction_by_md5";
        Map<String, String> body = Map.of("md5", qrMd5);

        BakongPaymentResponse apiResponse = bakongRestTemplate.postForObject(
                url, body, BakongPaymentResponse.class);

        //   NOT paid yet — just return order as-is, don't throw
        if (apiResponse == null || apiResponse.getResponseCode() != 0) {
            log.info("Payment not confirmed yet for order {}", id);
            return order; // still PENDING, frontend keeps polling
        }

        // Payment confirmed!
        BakongPaymentResponse.Data txn = apiResponse.getData();
        order.setPaid(true);
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        if (txn != null) {
            order.setBakongHash(txn.getHash());
            order.setFromAccountId(txn.getFromAccountId());
            order.setToAccountId(txn.getToAccountId());
        }

        return orderRepository.save(order);
    }
}