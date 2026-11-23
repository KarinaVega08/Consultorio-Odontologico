package com.consultorio.domain.availability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de solo lectura sobre los turnos existentes. C-05 lo implementa contra
 * su entidad {@code Turno}; C-04 solo necesita los intervalos ocupados.
 */
public interface TurnoDisponibilidadPort {

    List<Appointment> findTurnosOcupados(UUID profesionalId, LocalDate fecha, UUID sillonBoxId);
}
