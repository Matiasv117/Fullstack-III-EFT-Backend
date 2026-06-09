package com.saludrednorte.gateway.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final SimpleAuthService authService;

    public AuthController(SimpleAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin")
    public String admin() {
        return authService.generarToken("Marcelo", SimpleAuthService.ROLE_ADMIN);
    }

    @PostMapping("/user")
    public String user() {
        return authService.generarToken("Ana", SimpleAuthService.ROLE_USER);
    }
}

