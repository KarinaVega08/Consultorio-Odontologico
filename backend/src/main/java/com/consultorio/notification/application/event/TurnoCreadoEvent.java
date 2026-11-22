package com.consultorio.notification.application.event;

import com.consultorio.notification.domain.model.TurnoNotificable;

/**
 * Evento que C-05 publica cuando se crea un turno. Dispara el email de confirmación.
 */
public record TurnoCreadoEvent(TurnoNotificable turno) {
}
