package com.saludrednorte.ms_auth.service;

import com.saludrednorte.ms_auth.dto.LoginRequest;
import com.saludrednorte.ms_auth.dto.LoginResponse;
import com.saludrednorte.ms_auth.dto.PacienteDTO;
import com.saludrednorte.ms_auth.dto.PacienteLoginRequest;
import com.saludrednorte.ms_auth.dto.RegisterRequest;
import com.saludrednorte.ms_auth.repository.UserRepository;
import com.saludrednorte.ms_auth.client.PacienteClient;
import com.saludrednorte.ms_auth.security.JwtUtil;
import com.saludrednorte.ms_auth.util.RutUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Servicio de negocio para autenticación, registro y validación de usuarios.
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PacienteClient pacienteClient;

    /**
     * Autentica credenciales y genera un token JWT.
     *
     * @param loginRequest credenciales del usuario
     * @return respuesta con token y datos del usuario
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
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
                    .orElse("ROLE_USER");

            return new LoginResponse(token, userDetails.getUsername(), role);
        } catch (BadCredentialsException e) {
            throw e;
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request datos del usuario a registrar
     * @return respuesta con token JWT del usuario recién creado
     */
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        com.saludrednorte.ms_auth.entity.User user = new com.saludrednorte.ms_auth.entity.User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    /**
     * Valida un token JWT recibido en el header Authorization.
     *
     * @param authorizationHeader header con formato Bearer {token}
     * @return mapa con resultado de la validación
     */
    public Map<String, Object> validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token inválido");
        }

        String jwtToken = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(jwtToken, userDetails)) {
            throw new BadCredentialsException("Token inválido o expirado");
        }

        return Map.of("valid", true, "username", username);
    }

    /**
     * Obtiene información del usuario autenticado a partir del token JWT.
     *
     * @param authorizationHeader header con formato Bearer {token}
     * @return mapa con username, role y mensaje de confirmación
     */
    public Map<String, Object> getAuthenticatedUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("No autenticado");
        }

        String jwtToken = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(jwtToken, userDetails)) {
            throw new BadCredentialsException("Token inválido o expirado");
        }

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER");

        return Map.of(
                "username", username,
                "role", role,
                "message", "Acceso autorizado a endpoint privado"
        );
    }

    /**
     * Autentica un paciente usando sus datos personales (nombre, apellido, RUT).
     * Si el paciente no existe, lo crea automáticamente.
     * El RUT se normaliza automáticamente al formato estándar XX.XXX.XXX-X.
     *
     * @param request datos del paciente (nombre, apellido, RUT)
     * @return respuesta con token JWT y datos del paciente
     */
    public LoginResponse loginPaciente(PacienteLoginRequest request) {
        try {
            // Normalizar y formatear el RUT
            String rutNormalizado = RutUtil.normalizarRut(request.getRut());

            // Buscar paciente en ms-gestionpacientes (Feign lanza FeignException en 404)
            PacienteDTO paciente = null;
            try {
                paciente = pacienteClient.buscarPaciente(
                        request.getNombre(),
                        request.getApellido(),
                        rutNormalizado
                );
            } catch (FeignException fe) {
                // Si ms-gestionpacientes devuelve 404, Feign lanza excepción con status 404
                if (fe.status() == 404) {
                    paciente = null;
                } else {
                    // Propagar otras excepciones (500, 400, etc.) hacia arriba
                    throw fe;
                }
            }

            // Si el paciente no existe, crearlo
            if (paciente == null) {
                PacienteDTO nuevoPaciente = new PacienteDTO(
                        request.getNombre(),
                        request.getApellido(),
                        rutNormalizado
                );
                nuevoPaciente.setEmail(request.getEmail());
                paciente = pacienteClient.crearPaciente(nuevoPaciente);
            }

            // Crear UserDetails para el paciente
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    "PACIENTE_" + paciente.getId(),
                    "",
                    List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))
            );

            // Generar token JWT
            String token = jwtUtil.generateToken(userDetails);

            return new LoginResponse(token, "PACIENTE_" + paciente.getId(), "ROLE_PACIENTE");
        } catch (Exception e) {
            throw new BadCredentialsException("Error al autenticar paciente: " + e.getMessage());
        }
    }
}
