package com.consultorio.application.dto;

import java.time.LocalDateTime;

/**
 * Franja libre devuelta por la consulta de disponibilidad.
 */
public record AvailabilitySlotResponse(LocalDateTime inicio, LocalDateTime fin) {
}
