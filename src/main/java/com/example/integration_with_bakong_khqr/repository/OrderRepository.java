package com.example.integration_with_bakong_khqr.repository;

import com.example.integration_with_bakong_khqr.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Custom query to find an order by its QR MD5 hash
    // This is useful for the 'check_payment' logic
    Optional<Order> findByQrMd5(String qrMd5);
}