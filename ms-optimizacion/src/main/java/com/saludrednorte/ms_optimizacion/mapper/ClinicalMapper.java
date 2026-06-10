package com.saludrednorte.ms_optimizacion.mapper;

import com.saludrednorte.ms_optimizacion.dto.CitaDTO;
import com.saludrednorte.ms_optimizacion.dto.MedicoDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades y DTOs.
 */
@Component
public class ClinicalMapper {

    /**
     * Convierte una entidad Medico a MedicoDTO.
     */
    public MedicoDTO toMedicoDTO(Medico medico) {
        if (medico == null) {
            return null;
        }
        MedicoDTO dto = new MedicoDTO();
        dto.setId(medico.getId());
        dto.setNombre(medico.getNombre());
        dto.setEspecialidad(medico.getEspecialidad());
        return dto;
    }

    /**
     * Convierte MedicoDTO a entidad Medico.
     */
    public Medico toMedicoEntity(MedicoDTO dto) {
        if (dto == null) {
            return null;
        }
        Medico medico = new Medico();
        medico.setId(dto.getId());
        medico.setNombre(dto.getNombre());
        medico.setEspecialidad(dto.getEspecialidad());
        return medico;
    }

    /**
     * Convierte una entidad Cita a CitaDTO.
     */
    public CitaDTO toCitaDTO(Cita cita) {
        if (cita == null) {
            return null;
        }
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setPacienteId(cita.getPacienteId());
        dto.setMedico(toMedicoDTO(cita.getMedico()));
        dto.setFechaHora(cita.getFechaHora());
        dto.setEstado(cita.getEstado());
        return dto;
    }

    /**
     * Convierte CitaDTO a entidad Cita.
     */
    public Cita toCitaEntity(CitaDTO dto) {
        if (dto == null) {
            return null;
        }
        Cita cita = new Cita();
        cita.setId(dto.getId());
        cita.setPacienteId(dto.getPacienteId());
        cita.setMedico(toMedicoEntity(dto.getMedico()));
        cita.setFechaHora(dto.getFechaHora());
        cita.setEstado(dto.getEstado());
        return cita;
    }
}


