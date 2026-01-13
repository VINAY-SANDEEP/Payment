package com.gateway.config;

import java.time.Instant;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedTestMerchant(MerchantRepository merchantRepository) {
        return args -> {

            String testEmail = "test@example.com";

            // Skip if already exists
            if (merchantRepository.existsByEmail(testEmail)) {
                return;
            }

            Merchant merchant = new Merchant();
            merchant.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            merchant.setName("Test Merchant");
            merchant.setEmail(testEmail);
            merchant.setApiKey("key_test_abc123");
            merchant.setApiSecret("secret_test_xyz789");
            merchant.setIsActive(true);
            merchant.setCreatedAt(Instant.now());
            merchant.setUpdatedAt(Instant.now());

            merchantRepository.save(merchant);
        };
    }
}