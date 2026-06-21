package com.saludrednorte.ms_auditoria.service;

import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.dto.AuditLogResponse;
import com.saludrednorte.ms_auditoria.entity.AuditLog;
import com.saludrednorte.ms_auditoria.exception.AuditLogNotFoundException;
import com.saludrednorte.ms_auditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrarEvento_debePersistirYRetornarRespuesta() {
        AuditEventRequest request = new AuditEventRequest("admin", "LOGIN_EXITOSO", "Inicio de sesión correcto");
        AuditLog saved = new AuditLog("admin", "LOGIN_EXITOSO", "Inicio de sesión correcto");
        saved.setId(1L);
        saved.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        AuditLogResponse response = auditoriaService.registrarEvento(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getAction()).isEqualTo("LOGIN_EXITOSO");
    }

    @Test
    void obtenerPorId_debeRetornarEventoCuandoExiste() {
        AuditLog auditLog = new AuditLog("funcionario", "PACIENTE_REGISTRADO", "Paciente ID 10");
        auditLog.setId(5L);
        auditLog.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.findById(5L)).thenReturn(Optional.of(auditLog));

        AuditLogResponse response = auditoriaService.obtenerPorId(5L);

        assertThat(response.getAction()).isEqualTo("PACIENTE_REGISTRADO");
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriaService.obtenerPorId(99L))
                .isInstanceOf(AuditLogNotFoundException.class);
    }

    @Test
    void listarTodos_debeRetornarEventos() {
        AuditLog evento = new AuditLog("admin", "LOGIN_EXITOSO", "OK");
        evento.setId(1L);
        evento.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(evento));

        List<AuditLogResponse> result = auditoriaService.listarTodos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("LOGIN_EXITOSO");
    }

    @Test
    void listarPorUsuario_debeRetornarEventosFiltrados() {
        AuditLog evento = new AuditLog("admin", "LOGIN_EXITOSO", "OK");
        evento.setId(1L);
        evento.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.findByUsernameOrderByTimestampDesc("admin")).thenReturn(List.of(evento));

        List<AuditLogResponse> result = auditoriaService.listarPorUsuario("admin");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
    }

    @Test
    void listarPorAccion_debeRetornarEventosFiltrados() {
        AuditLog evento = new AuditLog("admin", "CITA_OPTIMIZADA", "Cita reasignada");
        evento.setId(2L);
        evento.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.findByActionOrderByTimestampDesc("CITA_OPTIMIZADA"))
                .thenReturn(List.of(evento));

        List<AuditLogResponse> result = auditoriaService.listarPorAccion("cita_optimizada");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("CITA_OPTIMIZADA");
    }
}