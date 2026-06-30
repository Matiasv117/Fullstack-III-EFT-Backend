package com.saludrednorte.ms_optimizacion.dto;

public class ReasignacionResponse {

    private Long citaId;
    private Long pacienteId;
    private String nombrePaciente;

    public ReasignacionResponse() {
    }

    public ReasignacionResponse(Long citaId, Long pacienteId, String nombrePaciente) {
        this.citaId = citaId;
        this.pacienteId = pacienteId;
        this.nombrePaciente = nombrePaciente;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
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
}
