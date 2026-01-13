package com.gateway.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gateway.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    // ✅ REQUIRED for Dashboard & Transactions
    List<Payment> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}