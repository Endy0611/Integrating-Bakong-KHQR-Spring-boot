package com.example.integration_with_bakong_khqr.model.entity;

import com.example.integration_with_bakong_khqr.constraint.OrderStatus;
import com.example.integration_with_bakong_khqr.constraint.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_md5", length = 32, unique = true)
    private String qrMd5;

    @Column(name = "qr_expiration")
    private Long qrExpiration;

    @Column(name = "bakong_hash", length = 255)
    private String bakongHash;

    @Column(name = "from_account_id", length = 100)
    private String fromAccountId;

    @Column(name = "to_account_id", length = 100)
    private String toAccountId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "paid")
    private Boolean paid = false;
}