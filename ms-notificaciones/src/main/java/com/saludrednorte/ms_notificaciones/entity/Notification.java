package com.saludrednorte.ms_notificaciones.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación en el sistema
 * Las notificaciones se crean cuando ocurren eventos relevantes
 * (citas confirmadas, canceladas, recordatorios, etc.)
 */
@Entity
@Table(name = "notificaciones")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pacienteId;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    @Column(length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    private EstadoNotificacion estado;

    private LocalDateTime creadoAt;

    private LocalDateTime enviadoAt;

    private Integer intentosEnvio;

    @Column(name = "email_destino")
    private String emailDestino;

    public Notification() {
        this.creadoAt = LocalDateTime.now();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.intentosEnvio = 0;
    }

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public LocalDateTime getCreadoAt() { return creadoAt; }
    public void setCreadoAt(LocalDateTime creadoAt) { this.creadoAt = creadoAt; }

    public LocalDateTime getEnviadoAt() { return enviadoAt; }
    public void setEnviadoAt(LocalDateTime enviadoAt) { this.enviadoAt = enviadoAt; }

    public Integer getIntentosEnvio() { return intentosEnvio; }
    public void setIntentosEnvio(Integer intentosEnvio) { this.intentosEnvio = intentosEnvio; }

    public String getEmailDestino() { return emailDestino; }
    public void setEmailDestino(String emailDestino) { this.emailDestino = emailDestino; }
}
