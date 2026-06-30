package com.saludrednorte.ms_notificaciones.messaging;

/**
 * Evento asíncrono recibido desde otros microservicios vía RabbitMQ.
 */
public class NotificacionEvent {

    private Long pacienteId;
    private String tipo;
    private String mensaje;
    private String origen;
    private String emailDestino;

    public NotificacionEvent() {
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

    public String getEmailDestino() {
        return emailDestino;
    }

    public void setEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }
}
