package com.saludrednorte.ms_notificaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_notificaciones.dto.NotificationMapper;
import com.saludrednorte.ms_notificaciones.dto.NotificationRequestDTO;
import com.saludrednorte.ms_notificaciones.dto.NotificationResponseDTO;
import com.saludrednorte.ms_notificaciones.entity.EstadoNotificacion;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.entity.TipoNotificacion;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests para NotificationController.
 * Valida los endpoints REST sin depender de WebMvcTest/MockBean.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService service;

    @Mock
    private NotificationMapper mapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private NotificationRequestDTO requestDTO;
    private NotificationResponseDTO responseDTO;
    private Notification notification;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(service, mapper)).build();

        requestDTO = new NotificationRequestDTO();
        requestDTO.setPacienteId(123L);
        requestDTO.setTipo(TipoNotificacion.CITA_CONFIRMADA);
        requestDTO.setMensaje("Su cita ha sido confirmada");

        notification = new Notification();
        notification.setId(1L);
        notification.setPacienteId(123L);
        notification.setTipo(TipoNotificacion.CITA_CONFIRMADA);
        notification.setMensaje("Su cita ha sido confirmada");
        notification.setEstado(EstadoNotificacion.PENDIENTE);
        notification.setCreadoAt(LocalDateTime.now());
        notification.setIntentosEnvio(0);

        responseDTO = new NotificationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPacienteId(123L);
        responseDTO.setTipo(TipoNotificacion.CITA_CONFIRMADA);
        responseDTO.setMensaje("Su cita ha sido confirmada");
        responseDTO.setEstado(EstadoNotificacion.PENDIENTE);
        responseDTO.setCreadoAt(LocalDateTime.now());
        responseDTO.setIntentosEnvio(0);
    }

    @Test
    void testCreateNotification() throws Exception {
        when(mapper.requestDtoToEntity(any(NotificationRequestDTO.class))).thenReturn(notification);
        when(service.create(any(Notification.class))).thenReturn(notification);
        when(mapper.entityToResponseDto(any(Notification.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(service).create(any(Notification.class));
    }

    @Test
    void testGetNotificationById() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(notification));
        when(mapper.entityToResponseDto(any(Notification.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetPendingNotifications() throws Exception {
        when(service.findPending()).thenReturn(List.of(notification));
        when(mapper.entityToResponseDto(any(Notification.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/notificaciones/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testSendNotification() throws Exception {
        when(service.sendById(1L)).thenReturn(true);

        mockMvc.perform(post("/api/notificaciones/1/enviar"))
                .andExpect(status().isOk());

        verify(service).sendById(1L);
    }

    @Test
    void testGetNotificationByIdNotFound() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notificaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetByPacienteId() throws Exception {
        when(service.findByPacienteId(123L)).thenReturn(List.of(notification));
        when(mapper.entityToResponseDto(any(Notification.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/notificaciones/paciente/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pacienteId").value(123));
    }

    @Test
    void testSendNotificationNotFound() throws Exception {
        when(service.sendById(99L)).thenReturn(false);

        mockMvc.perform(post("/api/notificaciones/99/enviar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEnviarPorCanal() throws Exception {
        when(service.sendById(1L, "EMAIL")).thenReturn(true);

        mockMvc.perform(post("/api/notificaciones/1/enviar-canal")
                        .param("canal", "EMAIL"))
                .andExpect(status().isOk());
    }

    @Test
    void testEnviarTodas() throws Exception {
        mockMvc.perform(post("/api/notificaciones/enviar-todas"))
                .andExpect(status().isOk());

        verify(service).sendPending();
    }

    @Test
    void testListarTodas() throws Exception {
        when(service.findAll()).thenReturn(List.of(notification));
        when(mapper.entityToResponseDto(any(Notification.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
