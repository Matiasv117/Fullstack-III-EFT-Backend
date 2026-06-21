package com.saludrednorte.ms_listas_espera.dto;

/**
 * DTO con métricas agregadas de la lista de espera calculadas vía stored procedure.
 */
public class ListaEsperaMetricasDTO {

    private long totalPendientes;
    private long pacientesGravedadAlta;
    private long pacientesGravedadMedia;
    private long pacientesGravedadBaja;

    public ListaEsperaMetricasDTO() {
    }

    public ListaEsperaMetricasDTO(long totalPendientes, long pacientesGravedadAlta,
                                  long pacientesGravedadMedia, long pacientesGravedadBaja) {
        this.totalPendientes = totalPendientes;
        this.pacientesGravedadAlta = pacientesGravedadAlta;
        this.pacientesGravedadMedia = pacientesGravedadMedia;
        this.pacientesGravedadBaja = pacientesGravedadBaja;
    }

    public long getTotalPendientes() {
        return totalPendientes;
    }

    public void setTotalPendientes(long totalPendientes) {
        this.totalPendientes = totalPendientes;
    }

    public long getPacientesGravedadAlta() {
        return pacientesGravedadAlta;
    }

    public void setPacientesGravedadAlta(long pacientesGravedadAlta) {
        this.pacientesGravedadAlta = pacientesGravedadAlta;
    }

    public long getPacientesGravedadMedia() {
        return pacientesGravedadMedia;
    }

    public void setPacientesGravedadMedia(long pacientesGravedadMedia) {
        this.pacientesGravedadMedia = pacientesGravedadMedia;
    }

    public long getPacientesGravedadBaja() {
        return pacientesGravedadBaja;
    }

    public void setPacientesGravedadBaja(long pacientesGravedadBaja) {
        this.pacientesGravedadBaja = pacientesGravedadBaja;
    }
}
