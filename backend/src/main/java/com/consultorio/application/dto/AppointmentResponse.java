package com.consultorio.application.dto;

import com.consultorio.domain.model.EstadoTurno;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación pública del turno; nunca se expone la entidad JPA.
 */
public record AppointmentResponse(
    UUID id,
    UUID pacienteId,
    UUID profesionalId,
    UUID sillonBoxId,
    UUID prestacionId,
    LocalDateTime fechaHoraInicio,
    LocalDateTime fechaHoraFin,
    EstadoTurno estado,
    String creadoPor,
    Instant fechaCreacion
) {
}
