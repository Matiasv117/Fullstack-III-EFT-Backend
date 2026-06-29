package com.saludrednorte.gateway.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleAuthServiceTest {

    private static final String JWT_SECRET = "miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte";

    private SimpleAuthService authService;

    @BeforeEach
    void setUp() {
        JwtTokenValidator validator = new JwtTokenValidator();
        org.springframework.test.util.ReflectionTestUtils.setField(validator, "secret", JWT_SECRET);
        authService = new SimpleAuthService(validator);
    }

    @Test
    void decodeBearerToken_validaJwtAdmin() {
        String jwt = buildJwt("admin", "ROLE_ADMIN");

        var decoded = authService.decodeBearerToken("Bearer " + jwt);

        assertThat(decoded).isPresent();
        assertThat(decoded.get().username()).isEqualTo("admin");
        assertThat(decoded.get().role()).isEqualTo("ADMIN");
    }

    @Test
    void decodeBearerToken_validaJwtUser() {
        String jwt = buildJwt("ana", "ROLE_USER");

        var decoded = authService.decodeBearerToken("Bearer " + jwt);

        assertThat(decoded).isPresent();
        assertThat(decoded.get().username()).isEqualTo("ana");
        assertThat(decoded.get().role()).isEqualTo("USER");
    }

    @Test
    void decodeBearerToken_mapeaFuncionarioComoUser() {
        String jwt = buildJwt("funcionario", "ROLE_FUNCIONARIO");

        var decoded = authService.decodeBearerToken("Bearer " + jwt);

        assertThat(decoded).isPresent();
        assertThat(decoded.get().role()).isEqualTo("USER");
    }

    @Test
    void canAccess_permiteAdminYRestringeUsuario() {
        assertThat(authService.canAccess("GET", "/productos", "ADMIN")).isTrue();
        assertThat(authService.canAccess("GET", "/productos", "USER")).isFalse();
        assertThat(authService.canAccess("GET", "/api/portal/resumen", "USER")).isTrue();
        assertThat(authService.canAccess("POST", "/pacientes", "USER")).isFalse();
    }

    private String buildJwt(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}
