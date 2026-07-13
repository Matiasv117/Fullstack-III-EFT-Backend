package com.saludrednorte.gateway.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenValidatorTest {

    private static final String JWT_SECRET = "miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte";

    private JwtTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator();
        ReflectionTestUtils.setField(validator, "secret", JWT_SECRET);
    }

    @Test
    void validate_tokenNull() {
        assertThat(validator.validate(null)).isEmpty();
    }

    @Test
    void validate_tokenBlank() {
        assertThat(validator.validate("")).isEmpty();
        assertThat(validator.validate("   ")).isEmpty();
    }

    @Test
    void validate_tokenExpirado() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_ADMIN");
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject("admin")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
                .setExpiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThat(validator.validate(jwt)).isEmpty();
    }

    @Test
    void validate_tokenMalformado() {
        assertThat(validator.validate("token-invalido")).isEmpty();
    }

    @Test
    void validate_tokenSinSubject() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_ADMIN");
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        assertThat(validator.validate(jwt)).isEmpty();
    }

    @Test
    void validate_tokenValido() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_ADMIN");
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        var result = validator.validate(jwt);
        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("admin");
        assertThat(result.get().role()).isEqualTo("ADMIN");
    }
}
