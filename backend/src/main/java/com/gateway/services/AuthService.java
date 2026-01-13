package com.gateway.services;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;

@Service
public class AuthService {

    private final MerchantRepository merchantRepository;

    public AuthService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Merchant authenticate(String apiKey, String apiSecret) {

        if (apiKey == null || apiSecret == null) {
            return null;
        }

        final String trimmedApiKey = apiKey.trim();
        final String trimmedApiSecret = apiSecret.trim();

        return merchantRepository.findByApiKey(trimmedApiKey)
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .filter(m -> Objects.equals(trimmedApiSecret, m.getApiSecret()))
                .orElse(null);
    }
}