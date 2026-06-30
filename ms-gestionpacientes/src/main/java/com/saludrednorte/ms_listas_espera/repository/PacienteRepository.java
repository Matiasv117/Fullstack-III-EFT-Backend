package com.saludrednorte.ms_listas_espera.repository;

import com.saludrednorte.ms_listas_espera.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    boolean existsByDniIgnoreCase(String dni);
		
		Optional<Paciente> findByNombreIgnoreCaseAndApellidoIgnoreCaseAndDniIgnoreCase(String nombre, String apellido, String dni);
		
		void deleteById(Long id);
}
