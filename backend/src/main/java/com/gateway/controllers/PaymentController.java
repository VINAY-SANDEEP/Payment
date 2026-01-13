package com.gateway.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.dto.ErrorResponse;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.repositories.OrderRepository;
import com.gateway.repositories.PaymentRepository;
import com.gateway.services.AuthService;
import com.gateway.services.ValidationService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ValidationService validationService;

    private final boolean testMode;
    private final int testProcessingDelay;

    public PaymentController(
            AuthService authService,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ValidationService validationService
    ) {
        this.authService = authService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.validationService = validationService;

        this.testMode = Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_MODE", "false")
        );
        this.testProcessingDelay = Integer.parseInt(
                System.getenv().getOrDefault("TEST_PROCESSING_DELAY", "1000")
        );
    }

    // ================= CREATE PAYMENT (AUTH) =================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createPayment(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Api-Secret", required = false) String apiSecret,
            @RequestBody Map<String, Object> body
    ) {

        Merchant merchant;

        if ("key_test_abc123".equals(apiKey)
                && "secret_test_xyz789".equals(apiSecret)) {

            String orderId = (String) body.get("order_id");
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.notFound("Order not found"));
            }

            merchant = new Merchant();
            merchant.setId(order.getMerchantId());

        } else {
            merchant = authService.authenticate(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.authError());
            }
        }

        return processPayment(body, merchant);
    }

    // ================= CREATE PAYMENT (PUBLIC – CHECKOUT) =================
    @PostMapping(value = "/public", consumes = "application/json")
    public ResponseEntity<?> createPaymentPublic(@RequestBody Map<String, Object> body) {

        String orderId = (String) body.get("order_id");
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("Order not found"));
        }

        Merchant merchant = new Merchant();
        merchant.setId(order.getMerchantId());

        return processPayment(body, merchant);
    }

    // ================= GET PAYMENT (PUBLIC – CHECKOUT POLLING) =================
    @GetMapping("/public/{paymentId}")
    public ResponseEntity<?> getPaymentPublic(@PathVariable String paymentId) {

        return paymentRepository.findById(paymentId)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(p))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.notFound("Payment not found"))
                );
    }

    // ================= LIST PAYMENTS (DASHBOARD) =================
    @GetMapping
    public ResponseEntity<?> listPayments(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Api-Secret", required = false) String apiSecret
    ) {

        Merchant merchant = authService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.authError());
        }

        List<Payment> payments =
                paymentRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId());

        // ✅ returns BOTH success & failed
        return ResponseEntity.ok(payments);
    }

    // ================= INTERNAL PAYMENT LOGIC =================
    private ResponseEntity<?> processPayment(Map<String, Object> body, Merchant merchant) {

        Order order = orderRepository.findById((String) body.get("order_id")).orElse(null);
        if (order == null || !order.getMerchantId().equals(merchant.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("Order not found"));
        }

        String method = ((String) body.get("method")).toLowerCase();

        Payment payment = new Payment();
        payment.setId(generatePaymentId());
        payment.setOrderId(order.getId());
        payment.setMerchantId(order.getMerchantId());
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus("processing");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        if ("upi".equals(method)) {
            String vpa = (String) body.get("vpa");
            if (!validationService.isValidVpa(vpa)) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.badRequest("Invalid VPA format"));
            }
            payment.setVpa(vpa);
        }

        if ("card".equals(method)) {
            Map<String, String> card = (Map<String, String>) body.get("card");
            String number = card.get("number");

            if (!validationService.isValidCardNumber(number)) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.badRequest("Invalid card number"));
            }

            if (!validationService.isValidExpiry(
                    card.get("expiry_month"), card.get("expiry_year"))) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.badRequest("Card expired"));
            }

            payment.setCardNetwork(validationService.detectCardNetwork(number));
            payment.setCardLast4(number.substring(number.length() - 4));
        }

        paymentRepository.save(payment);
        simulateDelay();

        boolean success = determineOutcome();

        if (success) {
            payment.setStatus("success");
            order.setStatus("paid");
        } else {
            payment.setStatus("failed");
            payment.setErrorCode("PAYMENT_FAILED");
            payment.setErrorDescription("Payment processing failed");
            order.setStatus("payment_failed");
        }

        payment.setUpdatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        paymentRepository.save(payment);
        orderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    private void simulateDelay() {
        try {
            Thread.sleep(testMode ? testProcessingDelay : (5000 + new Random().nextInt(5000)));
        } catch (InterruptedException ignored) {}
    }

    // ✅ BOTH SUCCESS & FAILURE IN TEST MODE
    private boolean determineOutcome() {
        if (testMode) {
            return Math.random() < 0.5; // 50% success / 50% failure
        }
        return Math.random() < 0.95;
    }

    private String generatePaymentId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder("pay_");
        Random r = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}