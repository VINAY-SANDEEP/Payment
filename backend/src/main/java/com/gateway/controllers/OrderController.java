package com.gateway.controllers;

import java.time.Instant;
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
import com.gateway.repositories.OrderRepository;
import com.gateway.services.AuthService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final AuthService authService;
    private final OrderRepository orderRepository;

    public OrderController(AuthService authService, OrderRepository orderRepository) {
        this.authService = authService;
        this.orderRepository = orderRepository;
    }

    // ================= CREATE ORDER =================
    @PostMapping(consumes = "application/json")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Api-Secret", required = false) String apiSecret,
            @RequestBody Map<String, Object> body
    ) {

        Merchant merchant = authService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.authError());
        }

        Object amountObj = body.get("amount");
        if (!(amountObj instanceof Number)) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("amount is required and must be a number"));
        }

        int amount = ((Number) amountObj).intValue();
        if (amount < 100) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("amount must be at least 100"));
        }

        String receipt = (String) body.get("receipt");
        if (receipt == null || receipt.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("receipt is required"));
        }

        Map<String, Object> notes = null;
        Object notesObj = body.get("notes");
        if (notesObj instanceof Map<?, ?>) {
            notes = (Map<String, Object>) notesObj;
        }

        Order order = new Order();
        order.setId(generateOrderId());
        order.setMerchantId(merchant.getId());
        order.setAmount(amount);
        order.setCurrency((String) body.getOrDefault("currency", "INR"));
        order.setReceipt(receipt);
        order.setNotes(notes);
        order.setStatus("created");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ================= GET ORDER (AUTHENTICATED) =================
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Api-Secret", required = false) String apiSecret,
            @PathVariable String orderId
    ) {

        Merchant merchant = authService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.authError());
        }

        return orderRepository.findById(orderId)
                .filter(o -> o.getMerchantId().equals(merchant.getId()))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.notFound("Order not found"))
                );
    }

    // ================= GET ORDER (PUBLIC – CHECKOUT) =================
    @GetMapping("/{orderId}/public")
    public ResponseEntity<?> getOrderPublic(@PathVariable String orderId) {

        return orderRepository.findById(orderId)
                .<ResponseEntity<?>>map(order -> ResponseEntity.ok(
                        Map.of(
                                "id", order.getId(),
                                "amount", order.getAmount(),
                                "currency", order.getCurrency(),
                                "status", order.getStatus()
                        )
                ))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.notFound("Order not found"))
                );
    }

    // ================= ORDER ID GENERATOR =================
    private String generateOrderId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder("order_");
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}