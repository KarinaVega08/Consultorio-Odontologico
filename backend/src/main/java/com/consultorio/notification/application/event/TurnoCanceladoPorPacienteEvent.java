package com.consultorio.notification.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento resultante de un enlace único de cancelación. C-05 lo consume para
 * cambiar el estado del turno a CANCELADO y liberar el horario.
 */
public record TurnoCanceladoPorPacienteEvent(UUID turnoId, LocalDateTime fechaCancelacion) {
}
