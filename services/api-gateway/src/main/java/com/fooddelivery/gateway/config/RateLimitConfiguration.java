package com.fooddelivery.gateway.config;

import java.security.Principal;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfiguration {

    @Bean
    public KeyResolver clientKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .map(userId -> "user:" + userId)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    if (exchange.getRequest().getRemoteAddress() == null
                            || exchange.getRequest().getRemoteAddress().getAddress() == null) {
                        return "anonymous:unknown";
                    }
                    return "ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                }));
    }
}
