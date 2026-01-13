package com.gateway.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final AuthService authService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public RefundController(
            AuthService authService,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.authService = authService;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> refundPayment(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Api-Secret", required = false) String apiSecret,
            @RequestBody Map<String, Object> body
    ) {

        // 1️⃣ Authenticate merchant
        Merchant merchant = authService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.authError());
        }

        // 2️⃣ Validate payment_id
        Object paymentIdObj = body.get("payment_id");
        if (!(paymentIdObj instanceof String) || ((String) paymentIdObj).isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("payment_id is required"));
        }

        String paymentId = (String) paymentIdObj;

        // 3️⃣ Fetch payment (merchant isolation)
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getMerchantId().equals(merchant.getId()))
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("Payment not found"));
        }

        // 4️⃣ Only successful payments can be refunded
        if (!"success".equals(payment.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("Only successful payments can be refunded"));
        }

        // 5️⃣ Prevent double refund
        if ("refunded".equals(payment.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("Payment already refunded"));
        }

        // 6️⃣ Fetch order
        Order order = orderRepository.findById(payment.getOrderId())
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("Order not found"));
        }

        // 7️⃣ Update statuses
        payment.setStatus("refunded");
        payment.setUpdatedAt(Instant.now());

        order.setStatus("refunded");
        order.setUpdatedAt(Instant.now());

        paymentRepository.save(payment);
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "payment_id", payment.getId(),
                "status", "refunded"
        ));
    }
}
