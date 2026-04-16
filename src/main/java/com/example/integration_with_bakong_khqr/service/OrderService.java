package com.example.integration_with_bakong_khqr.service;

import com.example.integration_with_bakong_khqr.model.entity.Order;

public interface OrderService {
    Order generateQRCode(Long id);

    Order checkPayment(Long id, String qrMd5);
}
