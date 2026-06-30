package com.saludrednorte.ms_notificaciones.service;

import com.saludrednorte.ms_notificaciones.entity.EstadoNotificacion;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de notificaciones.
 * <p>
 * Este servicio proporciona operaciones CRUD para notificaciones, incluyendo
 * envío de notificaciones por diferentes canales (EMAIL, SMS, PUSH).
 * </p>
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String CANAL_DEFECTO = "EMAIL";
    private static final List<String> CANALES_PERMITIDOS = List.of("EMAIL", "SMS", "PUSH");

    private final NotificationRepository repository;

    @Autowired
    private EmailService emailService;

    /**
     * Constructor del servicio.
     *
     * @param repository el repositorio de notificaciones
     */
    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea una nueva notificación.
     * <p>
     * Valida que no exista una notificación pendiente equivalente.
     * Establece el estado como PENDIENTE por defecto.
     * </p>
     *
     * @param notification la notificación a crear
     * @return la notificación creada con ID asignado
     * @throws IllegalArgumentException si la notificación es nula
     * @throws ResponseStatusException si ya existe una notificación pendiente equivalente
     */
    public Notification create(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("La notificación no puede ser nula");
        }
        if (repository.existsByPacienteIdAndTipoAndMensajeAndEstado(
                notification.getPacienteId(),
                notification.getTipo(),
                notification.getMensaje(),
                EstadoNotificacion.PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una notificación pendiente equivalente");
        }
        notification.setCreadoAt(notification.getCreadoAt() != null ? notification.getCreadoAt() : LocalDateTime.now());
        notification.setEstado(EstadoNotificacion.PENDIENTE);
        notification.setIntentosEnvio(notification.getIntentosEnvio() == null ? 0 : notification.getIntentosEnvio());
        return repository.save(notification);
    }

    /**
     * Obtiene todas las notificaciones pendientes de envío.
     *
     * @return lista de notificaciones pendientes
     */
    public List<Notification> findPending() {
        return repository.findByEstado(EstadoNotificacion.PENDIENTE);
    }

    /**
     * Obtiene todas las notificaciones de un paciente específico.
     *
     * @param pacienteId el ID del paciente
     * @return lista de notificaciones del paciente
     */
    public List<Notification> findByPacienteId(Long pacienteId) {
        return repository.findByPacienteId(pacienteId);
    }

    /**
     * Obtiene una notificación por su ID.
     *
     * @param id el ID de la notificación
     * @return Optional con la notificación si existe, vacío si no existe
     */
    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Obtiene todas las notificaciones del sistema.
     *
     * @return lista de todas las notificaciones
     */
    public List<Notification> findAll() {
        return repository.findAll();
    }

    /**
     * Envía todas las notificaciones pendientes.
     * <p>
     * Utiliza el canal por defecto (EMAIL) para enviar todas las notificaciones pendientes.
     * </p>
     */
    @Transactional
    public void sendPending() {
        findPending().forEach(notification -> markAsSent(notification, CANAL_DEFECTO));
    }

    /**
     * Envía una notificación específica por su ID.
     * <p>
     * Utiliza el canal por defecto (EMAIL) para enviar la notificación.
     * </p>
     *
     * @param id el ID de la notificación
     * @return true si se envió exitosamente, false si no existe
     */
    @Transactional
    public boolean sendById(Long id) {
        return sendById(id, CANAL_DEFECTO);
    }

    /**
     * Envía una notificación específica por su ID usando un canal específico.
     *
     * @param id el ID de la notificación
     * @param channel el canal de envío (EMAIL, SMS, PUSH)
     * @return true si se envió exitosamente, false si no existe
     * @throws IllegalArgumentException si el canal no es válido
     */
    @Transactional
    public boolean sendById(Long id, String channel) {
        String canalNormalizado = normalizeChannel(channel);
        Optional<Notification> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }

        markAsSent(opt.get(), canalNormalizado);
        return true;
    }

    /**
     * Obtiene la lista de canales de envío disponibles.
     *
     * @return lista de canales permitidos (EMAIL, SMS, PUSH)
     */
    public List<String> getAvailableChannels() {
        return CANALES_PERMITIDOS;
    }

    /**
     * Normaliza y valida el canal de envío.
     * <p>
     * Convierte el canal a mayúsculas y valida que sea un canal permitido.
     * </p>
     *
     * @param channel el canal a normalizar
     * @return el canal normalizado
     * @throws IllegalArgumentException si el canal no es válido
     */
    private String normalizeChannel(String channel) {
        String canal = channel == null ? CANAL_DEFECTO : channel.trim().toUpperCase();
        if (!CANALES_PERMITIDOS.contains(canal)) {
            throw new IllegalArgumentException("Canal no disponible: " + channel);
        }
        return canal;
    }

    /**
     * Marca una notificación como enviada.
     * <p>
     * Actualiza el estado a ENVIADA, registra la fecha de envío e incrementa el contador de intentos.
     * </p>
     *
     * @param notification la notificación a marcar
     * @param channel el canal utilizado para el envío
     */
    private void markAsSent(Notification notification, String channel) {
        notification.setEstado(EstadoNotificacion.ENVIADA);
        notification.setEnviadoAt(LocalDateTime.now());
        notification.setIntentosEnvio(notification.getIntentosEnvio() == null ? 1 : notification.getIntentosEnvio() + 1);
        repository.save(notification);
        logger.info("Notificación {} enviada por {}", notification.getId(), channel);

        if (notification.getEmailDestino() != null && !notification.getEmailDestino().isBlank()) {
            String subject = "RedNorte - " + notification.getTipo().name().replace("_", " ");
            String body = notification.getMensaje();
            emailService.sendEmail(notification.getEmailDestino(), subject, body);
        }
    }
}
