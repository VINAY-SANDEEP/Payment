package com.gateway.dto;

import java.util.Map;

public class ErrorResponse {

    // ========== AUTH ==========
    public static Map<String, Object> authError() {
        return Map.of(
                "error", Map.of(
                        "code", "AUTHENTICATION_ERROR",
                        "description", "Invalid API credentials"
                )
        );
    }

    // ========== GENERIC ==========
    public static Map<String, Object> badRequest(String message) {
        return Map.of(
                "error", Map.of(
                        "code", "BAD_REQUEST_ERROR",
                        "description", message
                )
        );
    }

    public static Map<String, Object> notFound(String message) {
        return Map.of(
                "error", Map.of(
                        "code", "NOT_FOUND_ERROR",
                        "description", message
                )
        );
    }

    // ========== PAYMENT VALIDATION (REQUIRED) ==========
    public static Map<String, Object> invalidVpa() {
        return Map.of(
                "error", Map.of(
                        "code", "INVALID_VPA",
                        "description", "VPA format invalid"
                )
        );
    }

    public static Map<String, Object> invalidCard() {
        return Map.of(
                "error", Map.of(
                        "code", "INVALID_CARD",
                        "description", "Card validation failed"
                )
        );
    }

    public static Map<String, Object> expiredCard() {
        return Map.of(
                "error", Map.of(
                        "code", "EXPIRED_CARD",
                        "description", "Card expiry date invalid"
                )
        );
    }

    public static Map<String, Object> paymentFailed() {
        return Map.of(
                "error", Map.of(
                        "code", "PAYMENT_FAILED",
                        "description", "Payment processing failed"
                )
        );
    }
}
