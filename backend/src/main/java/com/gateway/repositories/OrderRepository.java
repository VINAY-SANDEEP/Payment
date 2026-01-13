package com.gateway.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gateway.models.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByIdAndMerchantId(String id, UUID merchantId);

}
