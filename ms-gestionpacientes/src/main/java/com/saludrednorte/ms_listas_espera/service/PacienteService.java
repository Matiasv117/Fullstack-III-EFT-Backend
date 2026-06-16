package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.CitaDTO;
import com.saludrednorte.ms_listas_espera.dto.MedicoDTO;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private CitaClient citaClient;

    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }

        Paciente savedPaciente = pacienteRepository.save(paciente);

        // Crear cita automáticamente con el primer médico disponible
        try {
            List<MedicoDTO> medicos = citaClient.obtenerTodosMedicos();
            if (!medicos.isEmpty()) {
                MedicoDTO medico = medicos.get(0);
                CitaDTO cita = new CitaDTO();
                cita.setPacienteId(savedPaciente.getId());
                cita.setMedicoId(medico.getId());
                cita.setFechaHora(LocalDateTime.now().plusDays(1));
                cita.setEstado("CONFIRMADA");
                citaClient.crearCita(cita);
                logger.info("Cita creada automáticamente para paciente {} con médico {}", 
                           savedPaciente.getId(), medico.getId());
            }
        } catch (Exception e) {
            logger.warn("Fallo al crear cita automática pero paciente registrado: {}", e.getMessage());
        }

        // Crear notificación automáticamente
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(savedPaciente.getId());
            notif.setTipo("PACIENTE_ASIGNADO");
            notif.setMensaje("Paciente " + savedPaciente.getNombre() + " " +
                            savedPaciente.getApellido() + " registrado en el sistema");
            notificationClient.createNotification(notif);
            logger.info("Notificación creada para paciente {}", savedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al crear notificación pero paciente registrado: {}", e.getMessage());
        }

        return savedPaciente;
    }

    public List<Paciente> obtenerTodosPacientes() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente actualizarPaciente(Paciente paciente) {
        if (paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        Paciente updatedPaciente = pacienteRepository.save(paciente);

        // Notificar actualización
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(updatedPaciente.getId());
            notif.setTipo("ACTUALIZACION_ESTADO");
            notif.setMensaje("Datos del paciente " + updatedPaciente.getNombre() + " actualizados");
            notificationClient.createNotification(notif);
            logger.info("Notificación de actualización enviada para paciente {}", updatedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al notificar actualización pero paciente actualizado: {}", e.getMessage());
        }

        return updatedPaciente;
    }

    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        pacienteRepository.deleteById(id);
    }
}
