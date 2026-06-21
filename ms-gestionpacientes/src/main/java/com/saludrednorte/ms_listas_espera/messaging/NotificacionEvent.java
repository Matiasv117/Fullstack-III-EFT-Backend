package com.saludrednorte.ms_listas_espera.messaging;

/**
 * Evento asíncrono publicado cuando ocurre una acción que requiere notificación.
 */
public class NotificacionEvent {

    private Long pacienteId;
    private String tipo;
    private String mensaje;
    private String origen;

    public NotificacionEvent() {
    }

    public NotificacionEvent(Long pacienteId, String tipo, String mensaje, String origen) {
        this.pacienteId = pacienteId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.origen = origen;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
