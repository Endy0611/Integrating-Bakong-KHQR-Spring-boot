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
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{id}/generate_qrcode")
    public ResponseEntity<?> generateQrCode(@PathVariable Long id) {
        Order order = orderService.generateQRCode(id);
        // Using Map.of is great for simple success responses
        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "data", order
        ));
    }

    @PostMapping("/{id}/check_payment")
    public ResponseEntity<Order> checkPayment(
            @PathVariable Long id,
            @RequestBody CheckPaymentRequest request) {
        Order order = orderService.checkPayment(id, request.getQrMd5());
        return ResponseEntity.ok(order);
    }
}