package com.gateway.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gateway.models.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByApiKey(String apiKey);
    Optional<Merchant> findByEmail(String email);
    boolean existsByEmail(String email);
}