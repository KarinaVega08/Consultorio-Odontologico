package com.consultorio.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta de {@code GET /api/availability}. {@code franjas} está vacía cuando no hay disponibilidad.
 */
public record AvailabilityResponse(
    UUID profesionalId,
    LocalDate fecha,
    Integer duracionMinutos,
    List<AvailabilitySlotResponse> franjas
) {
}
