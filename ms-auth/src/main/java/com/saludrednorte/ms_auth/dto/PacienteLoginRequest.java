package com.saludrednorte.ms_auth.dto;

import com.saludrednorte.ms_auth.validation.RutValid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para el login de pacientes usando datos personales.
 */
public class PacienteLoginRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    private String apellido;

    @NotBlank(message = "El RUT es requerido")
    @RutValid(message = "RUT chileno inválido. Formato esperado: XX.XXX.XXX-X")
    private String rut;

    @Email(message = "Formato de correo inválido")
    private String email;

    public PacienteLoginRequest() {}

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
