package com.saludrednorte.ms_optimizacion.repository;

import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByEstado(EstadoCita estado);
    List<Cita> findByPacienteId(Long pacienteId);
    List<Cita> findByMedicoAndFechaHoraBetween(Medico medico, LocalDateTime start, LocalDateTime end);
    boolean existsByMedicoAndFechaHoraAndEstadoNot(Medico medico, LocalDateTime fechaHora, EstadoCita estado);
    void deleteByPacienteId(Long pacienteId);
}
