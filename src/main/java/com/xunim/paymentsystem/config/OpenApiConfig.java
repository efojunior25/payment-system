package com.xunim.paymentsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Xunim Payment System API")
                        .description("Sistema de gestão de pagamentos para portfolio")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Edson Junior")
                                .email("efojunior25@gmail.com")
                                .url("https://github.com/efojunior25"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Desenvolvimento Local"),
                        new Server().url("https://api.xunimpay.com").description("Produção")
                ));
    }
}