package com.fooddelivery.gateway.filter;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorrelationIdWebFilter implements WebFilter {

    private static final String HEADER_NAME = "X-Correlation-ID";
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String suppliedId = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        String correlationId = suppliedId != null && VALID_CORRELATION_ID.matcher(suppliedId).matches()
                ? suppliedId
                : UUID.randomUUID().toString();

        ServerWebExchange correlatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(HEADER_NAME, correlationId)))
                .build();
        correlatedExchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);
        return chain.filter(correlatedExchange);
    }
}
