package com.consultorio.domain.availability;

import java.time.LocalTime;

/**
 * Ventana de atención de un profesional para un día de semana (1 = lunes, 7 = domingo).
 */
public record WorkWindow(LocalTime horaInicio, LocalTime horaFin) {
}
