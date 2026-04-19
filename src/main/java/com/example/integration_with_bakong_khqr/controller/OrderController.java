package com.example.integration_with_bakong_khqr.controller;

import com.example.integration_with_bakong_khqr.model.entity.Order;
import com.example.integration_with_bakong_khqr.model.request.CheckPaymentRequest;
import com.example.integration_with_bakong_khqr.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // <-- Added this import

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")

@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/generate_qrcode") // no /{id} anymore
    public ResponseEntity<?> generateQrCode() {
        Order order = orderService.generateQRCode();
        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "data", order
        ));
    }

    @PostMapping("/{id}/check_payment") // keep id here — needed to find the order
    public ResponseEntity<Order> checkPayment(
            @PathVariable Long id,
            @RequestBody CheckPaymentRequest request) {
        Order order = orderService.checkPayment(id, request.getQrMd5());
        return ResponseEntity.ok(order);
    }
}