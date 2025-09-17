package com.xunim.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    @GetMapping
    @Operation(summary = "Health Check", description = "Verifica se a aplicação está funcionando")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            var healthComponent = healthEndpoint.health();

            if (healthComponent instanceof Health health) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", health.getStatus().equals(Status.UP) ? "UP" : "DOWN");
                response.put("timestamp", LocalDateTime.now());
                response.put("service", "XunimPay Payment System");
                response.put("version", "1.0.0");
                response.put("components", health.getClass());

                if (health.getStatus().equals(Status.UP)) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(503).body(response);
                }
            } else {
                // Caso não seja uma instância de Health
                Map<String, Object> response = new HashMap<>();
                response.put("status", healthComponent.getStatus().equals(Status.UP) ? "UP" : "DOWN");
                response.put("timestamp", LocalDateTime.now());
                response.put("service", "XunimPay Payment System");
                response.put("version", "1.0.0");
                response.put("components", Map.of());

                if (healthComponent.getStatus().equals(Status.UP)) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(503).body(response);
                }
            }
        } catch (Exception e) {
            log.error("Erro no health check", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("timestamp", LocalDateTime.now());
            response.put("service", "XunimPay Payment System");
            response.put("version", "1.0.0");
            response.put("error", "Health check failed");

            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/simple")
    @Operation(summary = "Simple Health Check", description = "Verificação simples de saúde")
    public ResponseEntity<Map<String, Object>> simpleHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "XunimPay Payment System");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }
}