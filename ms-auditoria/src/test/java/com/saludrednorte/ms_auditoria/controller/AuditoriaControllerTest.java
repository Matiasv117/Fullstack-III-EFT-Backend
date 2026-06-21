package com.saludrednorte.ms_auditoria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.dto.AuditLogResponse;
import com.saludrednorte.ms_auditoria.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditoriaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditoriaController auditoriaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditoriaController).build();
    }

    @Test
    void registrarEvento_debeRetornar201() throws Exception {
        AuditEventRequest request = new AuditEventRequest("admin", "LOGIN_EXITOSO", "OK");
        AuditLogResponse response = new AuditLogResponse(1L, "admin", "LOGIN_EXITOSO", "OK", LocalDateTime.now());
        when(auditoriaService.registrarEvento(any(AuditEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auditoria/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("LOGIN_EXITOSO"));
    }

    @Test
    void listarEventos_debeRetornarLista() throws Exception {
        AuditLogResponse evento = new AuditLogResponse(1L, "admin", "LOGIN_EXITOSO", "OK", LocalDateTime.now());
        when(auditoriaService.listarTodos()).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/auditoria/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    void obtenerEvento_debeRetornarEventoPorId() throws Exception {
        AuditLogResponse evento = new AuditLogResponse(5L, "funcionario", "PACIENTE_REGISTRADO", "ID 10", LocalDateTime.now());
        when(auditoriaService.obtenerPorId(5L)).thenReturn(evento);

        mockMvc.perform(get("/api/auditoria/eventos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void listarPorUsuario_debeRetornarEventosFiltrados() throws Exception {
        AuditLogResponse evento = new AuditLogResponse(2L, "admin", "LOGIN_EXITOSO", "OK", LocalDateTime.now());
        when(auditoriaService.listarPorUsuario("admin")).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/auditoria/eventos/usuario/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));

        verify(auditoriaService).listarPorUsuario("admin");
    }

    @Test
    void listarPorAccion_debeRetornarEventosFiltrados() throws Exception {
        AuditLogResponse evento = new AuditLogResponse(3L, "sistema", "CITA_OPTIMIZADA", "Cita 1", LocalDateTime.now());
        when(auditoriaService.listarPorAccion("CITA_OPTIMIZADA")).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/auditoria/eventos/accion/CITA_OPTIMIZADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CITA_OPTIMIZADA"));

        verify(auditoriaService).listarPorAccion("CITA_OPTIMIZADA");
    }
}
