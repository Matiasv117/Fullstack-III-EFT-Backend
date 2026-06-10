package com.saludrednorte.ms_optimizacion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Citas.
 * Expone solo los campos necesarios a través de la API REST.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CitaDTO {

    private Long id;
    private Long pacienteId;
    private MedicoDTO medico;
    private LocalDateTime fechaHora;
    private EstadoCita estado;

    // Constructores
    public CitaDTO() {
    }

    public CitaDTO(Long id, Long pacienteId, MedicoDTO medico, LocalDateTime fechaHora, EstadoCita estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medico = medico;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    // Getters and Setters
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

    public MedicoDTO getMedico() {
        return medico;
    }

    public void setMedico(MedicoDTO medico) {
        this.medico = medico;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
}


