package com.saludrednorte.ms_auth.service;

import com.saludrednorte.ms_auth.client.PacienteClient;
import com.saludrednorte.ms_auth.dto.LoginRequest;
import com.saludrednorte.ms_auth.dto.LoginResponse;
import com.saludrednorte.ms_auth.dto.PacienteDTO;
import com.saludrednorte.ms_auth.dto.PacienteLoginRequest;
import com.saludrednorte.ms_auth.dto.RegisterRequest;
import feign.FeignException;
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
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletResponse;

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

    @Mock
    private PacienteClient pacienteClient;

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
    void login_debePublicarEventoFallidoCuandoCredencialesSonInvalidas() {
        LoginRequest request = new LoginRequest("admin", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(auditEventPublisher).publicar("admin", "LOGIN_FALLIDO", "Credenciales inválidas");
    }

    @Test
    void validateToken_debeLanzarExcepcionCuandoTokenEstaExpirado() {
        when(jwtUtil.extractUsername("expired-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.validateToken("expired-token", userDetails)).thenReturn(false);

        assertThatThrownBy(() -> authService.validateToken("Bearer expired-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void getAuthenticatedUser_debeLanzarExcepcionCuandoTokenEsInvalido() {
        when(jwtUtil.extractUsername("bad-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.validateToken("bad-token", userDetails)).thenReturn(false);

        assertThatThrownBy(() -> authService.getAuthenticatedUser("Bearer bad-token"))
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

    @Test
    void loginPaciente_debeRetornarTokenCuandoExistePaciente() {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "12.345.678-5");
        PacienteDTO paciente = new PacienteDTO("Juan", "Perez", "12.345.678-5");
        paciente.setId(1L);

        when(pacienteClient.buscarPaciente("Juan", "Perez", "12.345.678-5")).thenReturn(paciente);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");

        LoginResponse response = authService.loginPaciente(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void loginPaciente_debeCrearPacienteSiNoExiste() {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "12.345.678-5");
        PacienteDTO nuevoPaciente = new PacienteDTO("Juan", "Perez", "12.345.678-5");
        nuevoPaciente.setId(1L);

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(notFound.status()).thenReturn(404);

        when(pacienteClient.buscarPaciente("Juan", "Perez", "12.345.678-5"))
                .thenThrow(notFound);
        when(pacienteClient.crearPaciente(any(PacienteDTO.class))).thenReturn(nuevoPaciente);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");

        LoginResponse response = authService.loginPaciente(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void loginPaciente_debePropagarFeignErrorNo404() {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "12.345.678-5");

        FeignException.InternalServerError ise = mock(FeignException.InternalServerError.class);
        when(ise.status()).thenReturn(500);

        when(pacienteClient.buscarPaciente("Juan", "Perez", "12.345.678-5"))
                .thenThrow(ise);

        assertThatThrownBy(() -> authService.loginPaciente(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Error al autenticar paciente");
    }

    @Test
    void loginPaciente_debeLanzarBadCredentialsCuandoRutEsInvalido() {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "xx");

        assertThatThrownBy(() -> authService.loginPaciente(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
