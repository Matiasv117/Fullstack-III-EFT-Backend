package com.saludrednorte.ms_optimizacion.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestDTOTest {

    private NotificationRequestDTO notification;

    @BeforeEach
    void setUp() {
        notification = new NotificationRequestDTO();
    }

    @Test
    void testConstructorVacio() {
        // Given
        NotificationRequestDTO dto = new NotificationRequestDTO();
        
        // Then
        assertNull(dto.getPacienteId());
        assertNull(dto.getTipo());
        assertNull(dto.getMensaje());
    }

    @Test
    void testConstructorConParametros() {
        // Given
        Long pacienteId = 100L;
        String tipo = "CITA_REASIGNADA";
        String mensaje = "Tu cita ha sido reasignada";
        
        // When
        NotificationRequestDTO dto = new NotificationRequestDTO(pacienteId, tipo, mensaje);
        
        // Then
        assertEquals(pacienteId, dto.getPacienteId());
        assertEquals(tipo, dto.getTipo());
        assertEquals(mensaje, dto.getMensaje());
    }

    @Test
    void testSetPacienteId() {
        // Given
        Long pacienteId = 200L;
        
        // When
        notification.setPacienteId(pacienteId);
        
        // Then
        assertEquals(pacienteId, notification.getPacienteId());
    }

    @Test
    void testSetTipo() {
        // Given
        String tipo = "CITA_CANCELADA";
        
        // When
        notification.setTipo(tipo);
        
        // Then
        assertEquals(tipo, notification.getTipo());
    }

    @Test
    void testSetMensaje() {
        // Given
        String mensaje = "Tu cita ha sido cancelada";
        
        // When
        notification.setMensaje(mensaje);
        
        // Then
        assertEquals(mensaje, notification.getMensaje());
    }

    @Test
    void testTodosCamposJuntos() {
        // Given
        Long pacienteId = 150L;
        String tipo = "RECORDATORIO";
        String mensaje = "Recordatorio de tu cita mañana";
        
        // When
        notification.setPacienteId(pacienteId);
        notification.setTipo(tipo);
        notification.setMensaje(mensaje);
        
        // Then
        assertEquals(pacienteId, notification.getPacienteId());
        assertEquals(tipo, notification.getTipo());
        assertEquals(mensaje, notification.getMensaje());
    }

    @Test
    void testValoresNulos() {
        // Given & When
        notification.setPacienteId(null);
        notification.setTipo(null);
        notification.setMensaje(null);
        
        // Then
        assertNull(notification.getPacienteId());
        assertNull(notification.getTipo());
        assertNull(notification.getMensaje());
    }

    @Test
    void testMultiplesInstancias() {
        // Given
        NotificationRequestDTO notif1 = new NotificationRequestDTO(1L, "TIPO1", "Mensaje1");
        NotificationRequestDTO notif2 = new NotificationRequestDTO(2L, "TIPO2", "Mensaje2");
        
        // When & Then
        assertEquals(1L, notif1.getPacienteId());
        assertEquals(2L, notif2.getPacienteId());
        assertNotEquals(notif1.getPacienteId(), notif2.getPacienteId());
    }

    @Test
    void testCamposVaciosConConstructor() {
        // Given
        NotificationRequestDTO dto = new NotificationRequestDTO(null, null, null);
        
        // Then
        assertNull(dto.getPacienteId());
        assertNull(dto.getTipo());
        assertNull(dto.getMensaje());
    }
}

