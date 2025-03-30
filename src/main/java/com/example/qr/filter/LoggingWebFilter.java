package com.example.qr.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
@Slf4j
public class LoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        long startTime = System.currentTimeMillis();

        log.info("START Request [{} {}]", method, path);

        return chain.filter(exchange)
            .doOnSuccess(aVoid -> {
                long duration = System.currentTimeMillis() - startTime;
                log.info("END Request [{} {}] completed in {} ms", method, path, duration);
            })
            .doOnError(e -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("END Request [{} {}] failed after {} ms with error: {}", method, path, duration, e.getMessage());
            });
    }
}
