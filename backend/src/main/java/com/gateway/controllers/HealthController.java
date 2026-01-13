package com.gateway.controllers;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", "healthy");

        try {
            // Actual DB connectivity check
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", "connected");
        } catch (DataAccessException ex) {
            response.put("database", "disconnected");
        }

        response.put("timestamp", Instant.now().toString());
        return response;
    }
}