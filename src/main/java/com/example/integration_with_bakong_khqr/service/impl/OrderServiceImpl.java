package com.example.integration_with_bakong_khqr.service.impl;

import com.example.integration_with_bakong_khqr.constraint.OrderStatus;
import com.example.integration_with_bakong_khqr.constraint.PaymentMethod;
import com.example.integration_with_bakong_khqr.model.entity.Order;
import com.example.integration_with_bakong_khqr.repository.OrderRepository;
import com.example.integration_with_bakong_khqr.service.OrderService;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRData;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order generateQRCode(Long id) {
        // 1. Find or create order
        Order order = orderRepository.findById(id).orElseGet(() -> {
            Order newOrder = new Order();
            newOrder.setAmount(new BigDecimal("0.10"));
            newOrder.setCurrency("USD");
            newOrder.setPaymentMethod(PaymentMethod.KHQR);
            newOrder.setStatus(OrderStatus.PENDING);
            newOrder.setPaid(false);
            return orderRepository.save(newOrder);
        });

        // 2. Calculate expiration timestamp (30 seconds from now)
        long expirationTimestamp = System.currentTimeMillis() + (30 * 1000);

        // 3. Setup Bakong KHQR Info
        IndividualInfo info = new IndividualInfo();
        info.setBakongAccountId("your_acc@bank");
        info.setMerchantName("Name");
        info.setMerchantCity("Phnom Penh");
        info.setAmount(order.getAmount().doubleValue());
        info.setCurrency(KHQRCurrency.USD);
        info.setExpirationTimestamp(expirationTimestamp); //Required for dynamic KHQR

        // 4. Generate QR using NBC SDK
        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(info);

        // 5. Update order with generated data
        if (response != null && response.getData() != null) {
            KHQRData data = response.getData();
            order.setQrCode(data.getQr());
            order.setQrMd5(data.getMd5());
            order.setQrExpiration(expirationTimestamp);
            return orderRepository.save(order);
        } else {
            if (response != null && response.getKHQRStatus() != null) {
                System.err.println("Bakong Error Code: " + response.getKHQRStatus().getCode());
                System.err.println("Bakong Error Msg: " + response.getKHQRStatus().getMessage());
            }
            throw new RuntimeException("Bakong SDK failed to generate QR. Check logs.");
        }
    }

    @Override
    @Transactional
    public Order checkPayment(Long id, String qrMd5) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        // Return immediately if already paid
        if (Boolean.TRUE.equals(order.getPaid()) && order.getStatus() == OrderStatus.PAID) {
            return order;
        }

        // Check if QR has expired
        if (order.getQrExpiration() != null && System.currentTimeMillis() > order.getQrExpiration()) {
            throw new RuntimeException("QR code has expired.");
        }

        // Verify MD5 hash and mark as paid
        if (qrMd5 != null && qrMd5.equals(order.getQrMd5())) {
            order.setPaid(true);
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            return orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid QR code hash verification.");
        }
    }
}