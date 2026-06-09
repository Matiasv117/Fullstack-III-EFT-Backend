package com.saludrednorte.ms_progreso.repository;

import com.saludrednorte.ms_progreso.entity.ProgresoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgresoRepository extends JpaRepository<ProgresoPaciente, Long> {
    Optional<ProgresoPaciente> findByPacienteId(Long pacienteId);
}

