package com.saludrednorte.bff.dto;

/**
 * DTO para el login de pacientes usando datos personales.
 */
public class PacienteLoginRequest {

    private String nombre;
    private String apellido;
    private String rut;
    private String email;

    public PacienteLoginRequest() {
    }

    public PacienteLoginRequest(String nombre, String apellido, String rut) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
