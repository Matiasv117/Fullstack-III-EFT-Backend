package com.saludrednorte.ms_progreso.dto;

import java.time.LocalDateTime;

/**
 * Respuesta con el estado actual del paciente.
 */
public class ProgresoResponse {

    private Long pacienteId;
    private String estado;
    private LocalDateTime fechaActualizacion;

    public ProgresoResponse(Long pacienteId, String estado, LocalDateTime fechaActualizacion) {
        this.pacienteId = pacienteId;
        this.estado = estado;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}

