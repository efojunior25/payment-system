package com.xunim.paymentsystem.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.xunim.paymentsystem.repository.mysql",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableMongoRepositories(
        basePackages = "com.xunim.paymentsystem.repository.mongodb"
)
@EntityScan("com.xunim.paymentsystem.entity")
@EnableJpaAuditing
@EnableMongoAuditing
@EnableTransactionManagement
public class DatabaseConfig {

}