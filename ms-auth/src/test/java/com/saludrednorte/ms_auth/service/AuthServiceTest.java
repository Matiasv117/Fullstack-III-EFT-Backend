package com.saludrednorte.ms_auth.service;

import com.saludrednorte.ms_auth.dto.LoginRequest;
import com.saludrednorte.ms_auth.dto.LoginResponse;
import com.saludrednorte.ms_auth.dto.RegisterRequest;
import com.saludrednorte.ms_auth.entity.User;
import com.saludrednorte.ms_auth.messaging.AuditEventPublisher;
import com.saludrednorte.ms_auth.repository.UserRepository;
import com.saludrednorte.ms_auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new org.springframework.security.core.userdetails.User(
                "admin",
                "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void login_debeRetornarTokenCuandoCredencialesSonValidas() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_debeCrearUsuarioYRetornarToken() {
        RegisterRequest request = new RegisterRequest("nuevo", "clave123", "ROLE_PACIENTE");

        when(userRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername("nuevo")).thenReturn(
                new org.springframework.security.core.userdetails.User(
                        "nuevo",
                        "encoded-pass",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))
                )
        );
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("new-token");

        LoginResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("new-token");
        assertThat(response.getUsername()).isEqualTo("nuevo");
        assertThat(response.getRole()).isEqualTo("ROLE_PACIENTE");
    }

    @Test
    void register_debeFallarSiUsuarioYaExiste() {
        RegisterRequest request = new RegisterRequest("admin", "admin123", "ROLE_ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User("admin", "x", "ROLE_ADMIN")));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre de usuario ya existe");
    }

    @Test
    void validateToken_debeRetornarValidoCuandoTokenEsCorrecto() {
        when(jwtUtil.extractUsername("valid-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid-token", userDetails)).thenReturn(true);

        Map<String, Object> result = authService.validateToken("Bearer valid-token");

        assertThat(result).containsEntry("valid", true);
        assertThat(result).containsEntry("username", "admin");
    }

    @Test
    void validateToken_debeLanzarExcepcionCuandoTokenEsInvalido() {
        assertThatThrownBy(() -> authService.validateToken("invalid"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void getAuthenticatedUser_debeRetornarDatosDelUsuario() {
        when(jwtUtil.extractUsername("valid-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid-token", userDetails)).thenReturn(true);

        Map<String, Object> result = authService.getAuthenticatedUser("Bearer valid-token");

        assertThat(result).containsEntry("username", "admin");
        assertThat(result).containsEntry("role", "ROLE_ADMIN");
    }
}
