package com.saludrednorte.gateway.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleAuthFilterTest {

    @Mock
    private SimpleAuthService authService;

    private SimpleAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SimpleAuthFilter(authService);
    }

    @Test
    void filter_publicPathPermiteAcceso() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        when(authService.isPublicPath(anyString())).thenReturn(true);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verify(authService).isPublicPath(anyString());
    }

    @Test
    void filter_sinTokenRetorna401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/productos")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = mock(WebFilterChain.class);

        when(authService.isPublicPath(anyString())).thenReturn(false);
        when(authService.decodeBearerToken(any())).thenReturn(Optional.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_tokenValidoSinAccesoRetorna403() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/productos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-valido")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = mock(WebFilterChain.class);

        when(authService.isPublicPath(anyString())).thenReturn(false);
        when(authService.decodeBearerToken("Bearer token-valido")).thenReturn(
                Optional.of(new SimpleAuthService.TokenData("user", "USER")));
        when(authService.canAccess(anyString(), anyString(), anyString())).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_tokenValidoConAccesoPermite() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/pacientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-valido")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        when(authService.isPublicPath(anyString())).thenReturn(false);
        when(authService.decodeBearerToken("Bearer token-valido")).thenReturn(
                Optional.of(new SimpleAuthService.TokenData("admin", "ADMIN")));
        when(authService.canAccess(anyString(), anyString(), anyString())).thenReturn(true);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }
}
