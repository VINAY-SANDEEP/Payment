package com.gateway.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.dto.ErrorResponse;
import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final MerchantRepository merchantRepository;

    public TestController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @GetMapping("/merchant")
    public ResponseEntity<?> testMerchant() {

        UUID testMerchantId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        return merchantRepository.findById(testMerchantId)
                .<ResponseEntity<?>>map(m -> ResponseEntity.ok(
                        Map.of(
                                "id", m.getId().toString(),
                                "email", m.getEmail(),
                                "api_key", m.getApiKey(),
                                "seeded", true
                        )
                ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.notFound("Test merchant not found"))
                );
    }
}