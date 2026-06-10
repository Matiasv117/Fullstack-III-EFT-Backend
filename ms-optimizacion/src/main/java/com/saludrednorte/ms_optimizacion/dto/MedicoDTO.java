package com.saludrednorte.ms_optimizacion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para transferencia de datos de Médicos.
 * Expone solo los campos necesarios a través de la API REST.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicoDTO {

    private Long id;
    private String nombre;
    private String especialidad;

    // Constructores
    public MedicoDTO() {
    }

    public MedicoDTO(Long id, String nombre, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}


