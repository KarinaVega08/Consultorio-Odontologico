package com.consultorio.domain.availability;

import java.time.LocalDateTime;

/**
 * Franja libre candidata a reserva.
 */
public record AvailabilitySlot(LocalDateTime inicio, LocalDateTime fin) {
}
