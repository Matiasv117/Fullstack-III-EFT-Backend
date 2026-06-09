package com.saludrednorte.ms_listas_espera.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TipoNotificacionTest {

    @Test
    void testCitaConfirmada() {
        TipoNotificacion tipo = TipoNotificacion.CITA_CONFIRMADA;
        assertEquals("Confirmación de cita médica asignada", tipo.getDescripcion());
    }

    @Test
    void testCitaCancelada() {
        TipoNotificacion tipo = TipoNotificacion.CITA_CANCELADA;
        assertEquals("Notificación de cancelación de cita", tipo.getDescripcion());
    }

    @Test
    void testRecordatorioCita() {
        TipoNotificacion tipo = TipoNotificacion.RECORDATORIO_CITA;
        assertEquals("Recordatorio de cita próxima", tipo.getDescripcion());
    }

    @Test
    void testCambioHorario() {
        TipoNotificacion tipo = TipoNotificacion.CAMBIO_HORARIO;
        assertEquals("Cambio de horario de cita", tipo.getDescripcion());
    }

    @Test
    void testPacienteAsignado() {
        TipoNotificacion tipo = TipoNotificacion.PACIENTE_ASIGNADO;
        assertEquals("Paciente asignado a lista de espera", tipo.getDescripcion());
    }

    @Test
    void testCambioPrioridad() {
        TipoNotificacion tipo = TipoNotificacion.CAMBIO_PRIORIDAD;
        assertEquals("Cambio de prioridad en lista de espera", tipo.getDescripcion());
    }

    @Test
    void testPosicionActualizada() {
        TipoNotificacion tipo = TipoNotificacion.POSICION_ACTUALIZADA;
        assertEquals("Actualización de posición en lista de espera", tipo.getDescripcion());
    }

    @Test
    void testActualizacionEstado() {
        TipoNotificacion tipo = TipoNotificacion.ACTUALIZACION_ESTADO;
        assertEquals("Actualización de estado en lista de espera", tipo.getDescripcion());
    }

    @Test
    void testEliminacionListaEspera() {
        TipoNotificacion tipo = TipoNotificacion.ELIMINACION_LISTA_ESPERA;
        assertEquals("Eliminación de paciente de lista de espera", tipo.getDescripcion());
    }

    @Test
    void testValues() {
        TipoNotificacion[] values = TipoNotificacion.values();
        assertEquals(9, values.length);
    }

    @Test
    void testValueOf() {
        TipoNotificacion tipo = TipoNotificacion.valueOf("CITA_CONFIRMADA");
        assertEquals(TipoNotificacion.CITA_CONFIRMADA, tipo);
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            TipoNotificacion.valueOf("TIPO_INEXISTENTE");
        });
    }
}
