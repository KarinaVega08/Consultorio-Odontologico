package com.consultorio.notification.application.event;

import com.consultorio.notification.domain.model.TurnoNotificable;

/**
 * Evento que C-05 publica cuando se cancela un turno desde la operatoria interna.
 */
public record TurnoCanceladoEvent(TurnoNotificable turno) {
}
