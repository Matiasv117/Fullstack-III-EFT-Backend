package com.saludrednorte.ms_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_auth.entity.User;
import com.saludrednorte.ms_auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminController adminController;

    private static final String ADMIN_TOKEN = "Bearer eyJzdWIiOiJhZG1pbiJ9.eyJzdWIiOiJhZG1pbiJ9.dGVzdA";
    private static final String NON_ADMIN_TOKEN = "Bearer eyJzdWIiOiJ1c3VhcmlvIn0.eyJzdWIiOiJ1c3VhcmlvIn0.dGVzdA";
    private static final String MALFORMED_TOKEN = "Bearer invalid";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void listarFuncionarios_debeRetornarLista() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(
                new User("admin", "x", "ROLE_ADMIN"),
                new User("func1", "x", "ROLE_FUNCIONARIO"),
                new User("func2", "x", "ROLE_FUNCIONARIO")
        ));

        mockMvc.perform(get("/api/admin/funcionarios")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void listarFuncionarios_debeRetornar403CuandoNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/funcionarios")
                        .header("Authorization", NON_ADMIN_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Solo administradores pueden acceder"));
    }

    @Test
    void listarFuncionarios_debeRetornar403CuandoTokenEsInvalido() throws Exception {
        mockMvc.perform(get("/api/admin/funcionarios")
                        .header("Authorization", MALFORMED_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarFuncionarios_debeRetornar400SinToken() throws Exception {
        mockMvc.perform(get("/api/admin/funcionarios"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearFuncionario_debeRetornar201() throws Exception {
        Map<String, String> body = Map.of(
                "username", "nuevo",
                "password", "clave123",
                "nombreCompleto", "Nuevo Usuario",
                "email", "nuevo@test.cl"
        );

        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        mockMvc.perform(post("/api/admin/funcionarios")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo"));
    }

    @Test
    void crearFuncionario_debeRetornar409SiYaExiste() throws Exception {
        Map<String, String> body = Map.of("username", "existente", "password", "clave");

        when(userRepository.existsByUsername("existente")).thenReturn(true);

        mockMvc.perform(post("/api/admin/funcionarios")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El username ya existe"));
    }

    @Test
    void crearFuncionario_debeRetornar403CuandoNoEsAdmin() throws Exception {
        Map<String, String> body = Map.of("username", "nuevo", "password", "clave");

        mockMvc.perform(post("/api/admin/funcionarios")
                        .header("Authorization", NON_ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarFuncionario_debeRetornar200() throws Exception {
        User existing = new User("func1", "encoded", "ROLE_FUNCIONARIO");
        existing.setId(1L);

        Map<String, String> body = Map.of("nombreCompleto", "Modificado");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/admin/funcionarios/1")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("func1"));
    }

    @Test
    void modificarFuncionario_debeRetornar404SiNoExiste() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/funcionarios/99")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombreCompleto", "X"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Funcionario no encontrado"));
    }

    @Test
    void modificarFuncionario_debeRetornar403SiEsAdmin() throws Exception {
        User admin = new User("admin", "x", "ROLE_ADMIN");
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        mockMvc.perform(put("/api/admin/funcionarios/1")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombreCompleto", "X"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No se puede modificar al administrador principal"));
    }

    @Test
    void eliminarFuncionario_debeRetornar200() throws Exception {
        User func = new User("func1", "x", "ROLE_FUNCIONARIO");
        func.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(func));

        mockMvc.perform(delete("/api/admin/funcionarios/1")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Funcionario eliminado exitosamente"));

        verify(userRepository).deleteById(1L);
    }

    @Test
    void eliminarFuncionario_debeRetornar404SiNoExiste() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/admin/funcionarios/99")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarFuncionario_debeRetornar403SiEsAdmin() throws Exception {
        User admin = new User("admin", "x", "ROLE_ADMIN");
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        mockMvc.perform(delete("/api/admin/funcionarios/1")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarEstado_debeRetornar200() throws Exception {
        User func = new User("func1", "x", "ROLE_FUNCIONARIO");
        func.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(func));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/admin/funcionarios/1/estado")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("activo", false))))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_debeRetornar404SiNoExiste() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/funcionarios/99/estado")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("activo", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarRol_debeRetornar200() throws Exception {
        User func = new User("func1", "x", "ROLE_FUNCIONARIO");
        func.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(func));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/admin/funcionarios/1/rol")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rol", "ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarRol_debeRetornar400SiRolEsInvalido() throws Exception {
        mockMvc.perform(put("/api/admin/funcionarios/1/rol")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rol", "ROLE_INVALIDO"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rol inválido"));
    }

    @Test
    void cambiarRol_debeRetornar404SiNoExiste() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/funcionarios/99/rol")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rol", "ROLE_FUNCIONARIO"))))
                .andExpect(status().isNotFound());
    }
}
