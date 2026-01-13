package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantRepository merchantRepository;

    public MerchantController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @PostMapping
    public ResponseEntity<?> createMerchant(@RequestBody Map<String, Object> body) {

        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String webhookUrl = (String) body.get("webhook_url");

        if (name == null || email == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "name and email are required")
            );
        }

        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID());
        merchant.setName(name);
        merchant.setEmail(email);
        merchant.setWebhookUrl(webhookUrl);
        merchant.setApiKey("key_" + UUID.randomUUID().toString().replace("-", ""));
        merchant.setApiSecret("secret_" + UUID.randomUUID().toString().replace("-", ""));
        merchant.setIsActive(true);
        merchant.setCreatedAt(Instant.now());
        merchant.setUpdatedAt(Instant.now());

        merchantRepository.save(merchant);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", merchant.getId(),
                        "name", merchant.getName(),
                        "email", merchant.getEmail(),
                        "api_key", merchant.getApiKey(),
                        "api_secret", merchant.getApiSecret()
                )
        );
    }
}