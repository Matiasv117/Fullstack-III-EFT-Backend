package com.saludrednorte.gateway.auth;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleAuthServiceTest {

    private final SimpleAuthService authService = new SimpleAuthService();

    @Test
    void generarToken_creaBase64Esperado() {
        String token = authService.generarToken("Marcelo", SimpleAuthService.ROLE_ADMIN);

        assertThat(token).isEqualTo(Base64.getEncoder().encodeToString("Marcelo:ADMIN".getBytes()));
    }

    @Test
    void decodeBearerToken_recuperaUsuarioYRol() {
        String token = authService.generarToken("Ana", SimpleAuthService.ROLE_USER);

        var decoded = authService.decodeBearerToken("Bearer " + token);

        assertThat(decoded).isPresent();
        assertThat(decoded.get().username()).isEqualTo("Ana");
        assertThat(decoded.get().role()).isEqualTo("USER");
    }

    @Test
    void canAccess_permiteAdminYRestringeUsuario() {
        assertThat(authService.canAccess("GET", "/productos", "ADMIN")).isTrue();
        assertThat(authService.canAccess("GET", "/productos", "USER")).isFalse();
        assertThat(authService.canAccess("GET", "/api/portal/resumen", "USER")).isTrue();
        assertThat(authService.canAccess("POST", "/pacientes", "USER")).isFalse();
    }
}

