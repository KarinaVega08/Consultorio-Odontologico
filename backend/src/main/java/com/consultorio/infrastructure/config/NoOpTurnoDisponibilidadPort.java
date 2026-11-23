package com.consultorio.infrastructure.config;

import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Fallback mientras C-05 no aporta la implementación real: no hay turnos que
 * ocupen la agenda, por lo que solo cuentan horarios y bloqueos.
 */
public class NoOpTurnoDisponibilidadPort implements TurnoDisponibilidadPort {

    @Override
    public List<Appointment> findTurnosOcupados(UUID profesionalId, LocalDate fecha, UUID sillonBoxId) {
        return List.of();
    }
}
