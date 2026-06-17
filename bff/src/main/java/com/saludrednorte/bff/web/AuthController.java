package com.saludrednorte.bff.web;

import com.saludrednorte.bff.dto.LoginRequest;
import com.saludrednorte.bff.dto.LoginResponse;
import com.saludrednorte.bff.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación para login y generación de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Endpoint de login que genera un token JWT.
     *
     * @param loginRequest credenciales del usuario
     * @return respuesta con el token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("USER");

            LoginResponse response = new LoginResponse(token, userDetails.getUsername(), role);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    /**
     * Endpoint para validar un token JWT.
     *
     * @param token token JWT a validar
     * @return respuesta indicando si el token es válido
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String username = jwtUtil.extractUsername(jwtToken);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                boolean isValid = jwtUtil.validateToken(jwtToken, userDetails);
                
                if (isValid) {
                    return ResponseEntity.ok().body("Token válido");
                }
            }
            return ResponseEntity.status(401).body("Token inválido o expirado");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error al validar token");
        }
    }

    /**
     * Endpoint privado de prueba para verificar autenticación JWT.
     * Requiere token válido en el header Authorization.
     *
     * @return información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("USER");
                
                return ResponseEntity.ok(Map.of(
                    "username", username,
                    "role", role,
                    "message", "Acceso autorizado a endpoint privado"
                ));
            }
            
            return ResponseEntity.status(401).body("No autenticado");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error al obtener usuario autenticado");
        }
    }
}
