package com.consultorio.notification.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento resultante de un enlace único de confirmación de asistencia.
 */
public record AsistenciaConfirmadaEvent(UUID turnoId, LocalDateTime fechaConfirmacion) {
}
