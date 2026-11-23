package com.consultorio.domain.repository;

import com.consultorio.domain.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la gestión de pacientes.
 */
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByDni(String dni);

    boolean existsByDni(String dni);
}
