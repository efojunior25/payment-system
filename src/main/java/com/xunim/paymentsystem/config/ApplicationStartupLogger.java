package com.xunim.paymentsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupLogger {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String baseUrl = "http://localhost:" + serverPort + contextPath;

        log.info("=".repeat(80));
        log.info("🚀 XunimPay Payment System está rodando!");
        log.info("📊 Profile ativo: {}", activeProfile);
        log.info("🌐 URL base: {}", baseUrl);
        log.info("📖 Swagger UI: {}/swagger-ui.html", baseUrl);
        log.info("💚 Health Check: {}/health", baseUrl);
        log.info("👤 Usuários: {}/v1/users", baseUrl);
        log.info("💰 Contas: {}/v1/accounts", baseUrl);
        log.info("💸 Pagamentos: {}/v1/payments", baseUrl);
        log.info("=".repeat(80));
    }
}