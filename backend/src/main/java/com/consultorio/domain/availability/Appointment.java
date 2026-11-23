package com.consultorio.domain.availability;

import com.consultorio.domain.model.EstadoTurno;

import java.time.LocalDateTime;

/**
 * Turno ya existente que ocupa un intervalo de la agenda.
 * Los turnos {@code CANCELADO} liberan el hueco y se ignoran en el cálculo.
 */
public record Appointment(LocalDateTime inicio, LocalDateTime fin, EstadoTurno estado) {
}
