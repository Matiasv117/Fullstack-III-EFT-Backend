package com.saludrednorte.gateway.auth;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SimpleAuthFilter implements GlobalFilter, Ordered {

    private final SimpleAuthService authService;

    public SimpleAuthFilter(SimpleAuthService authService) {
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : HttpMethod.GET.name();
        String path = exchange.getRequest().getPath().value();

        if (HttpMethod.OPTIONS.matches(method) || authService.isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        var tokenData = authService.decodeBearerToken(authorizationHeader);
        if (tokenData.isEmpty()) {
            return unauthorized(exchange);
        }

        if (!authService.canAccess(method, path, tokenData.get().role())) {
            return forbidden(exchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}


