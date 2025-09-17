package com.xunim.paymentsystem.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(
                        org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues()
                )
                .build();
    }
}