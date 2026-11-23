package com.consultorio.notification.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Datos mínimos de un turno para notificar. No es una entidad JPA: evita acoplar
 * este change con la entidad {@code Turno} que se implementa en C-05.
 */
public record TurnoNotificable(
    UUID turnoId,
    UUID pacienteId,
    String dniPaciente,
    String emailPaciente,
    String profesionalNombre,
    LocalDateTime fechaHoraInicio,
    String prestacionNombre
) {
}
