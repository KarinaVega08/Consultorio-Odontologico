package com.consultorio.application.dto;

import com.consultorio.domain.model.EstadoTurno;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros opcionales para {@code GET /api/appointments}.
 */
public record AppointmentQuery(UUID profesionalId, LocalDate fecha, EstadoTurno estado) {
}
