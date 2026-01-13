package com.gateway.services;

import java.time.YearMonth;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    // ===== VPA VALIDATION =====
    private static final Pattern VPA_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    public boolean isValidVpa(String vpa) {
        if (vpa == null) return false;
        return VPA_PATTERN.matcher(vpa.trim()).matches();
    }

    // ===== CARD NUMBER (LUHN) =====
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;

        String digits = cardNumber.trim().replaceAll("[^0-9]", "");
        if (digits.length() < 13 || digits.length() > 19) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(digits.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    // ===== CARD NETWORK DETECTION =====
    public String detectCardNetwork(String cardNumber) {
        if (cardNumber == null) return "unknown";

        String digits = cardNumber.trim().replaceAll("[^0-9]", "");

        if (digits.startsWith("4")) return "visa";

        if (digits.length() >= 2) {
            int prefix2 = Integer.parseInt(digits.substring(0, 2));

            if (prefix2 >= 51 && prefix2 <= 55) return "mastercard";
            if (prefix2 == 34 || prefix2 == 37) return "amex";
            if (prefix2 == 60 || prefix2 == 65) return "rupay";
            if (prefix2 >= 81 && prefix2 <= 89) return "rupay";
        }

        return "unknown";
    }

    // ===== CARD EXPIRY VALIDATION =====
    public boolean isValidExpiry(String monthStr, String yearStr) {
        if (monthStr == null || yearStr == null) return false;

        try {
            int month = Integer.parseInt(monthStr.trim());
            int year = Integer.parseInt(yearStr.trim());

            if (month < 1 || month > 12) return false;

            if (yearStr.trim().length() == 2) {
                year += 2000;
            }

            YearMonth expiry = YearMonth.of(year, month);
            YearMonth now = YearMonth.now();

            return !expiry.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }
}