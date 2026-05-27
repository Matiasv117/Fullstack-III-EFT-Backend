package com.saludrednorte.ms_optimizacion.dto;

/**
 * Respuesta simple con el nivel de prioridad calculado.
 */
public class PrioridadResponse {

    private String nivel;

    public PrioridadResponse() {
    }

    public PrioridadResponse(String nivel) {
        this.nivel = nivel;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}

