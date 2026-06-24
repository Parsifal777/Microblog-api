package com.microblog.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    // Хранилище для бакетов по IP или userId
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public ConcurrentHashMap<String, Bucket> buckets() {
        return buckets;
    }

    // Создать бакет для пользователя (10 запросов в минуту)
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Получить или создать бакет для пользователя
    public Bucket getBucket(String userId) {
        return buckets.computeIfAbsent(userId, key -> createBucket());
    }
}
