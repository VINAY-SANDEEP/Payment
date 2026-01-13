package com.gateway;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class PaymentGatewayApplication {

    private final MerchantRepository merchantRepository;

    public PaymentGatewayApplication(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayApplication.class, args);
    }

    @PostConstruct
    public void seedMerchant() {
        if (!merchantRepository.existsByEmail("test@example.com")) {
            Merchant m = new Merchant();
            m.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            m.setName("Test Merchant");
            m.setEmail("test@example.com");
            m.setApiKey("key_test_abc123");
            m.setApiSecret("secret_test_xyz789");
            merchantRepository.save(m);
        }
    }
}
