package com.saludrednorte.ms_optimizacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ListaEsperaDTO {

    private Long id;
    private Long pacienteId;
    private String nombrePaciente;
    private String interconsulta;
    private String gravedad;
    private String estado;

    public ListaEsperaDTO() {
    }

    public ListaEsperaDTO(Long id, Long pacienteId, String interconsulta, String gravedad, String estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.interconsulta = interconsulta;
        this.gravedad = gravedad;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    @JsonProperty("paciente")
    public void unpackPaciente(Map<String, Object> paciente) {
        if (paciente != null) {
            this.pacienteId = paciente.get("id") != null ? ((Number) paciente.get("id")).longValue() : null;
            String nombre = (String) paciente.getOrDefault("nombre", "");
            String apellido = (String) paciente.getOrDefault("apellido", "");
            this.nombrePaciente = (nombre + " " + apellido).trim();
        }
    }

    public String getInterconsulta() {
        return interconsulta;
    }

    public void setInterconsulta(String interconsulta) {
        this.interconsulta = interconsulta;
    }

    public String getGravedad() {
        return gravedad;
    }

    public void setGravedad(String gravedad) {
        this.gravedad = gravedad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
