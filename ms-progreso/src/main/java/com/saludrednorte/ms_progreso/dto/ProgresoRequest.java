package com.saludrednorte.ms_progreso.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para crear o actualizar el estado de progreso.
 */
public class ProgresoRequest {

    @NotBlank
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

