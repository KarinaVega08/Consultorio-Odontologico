package com.consultorio.notification.application.event;

import com.consultorio.notification.domain.model.TurnoNotificable;

import java.time.LocalDateTime;

/**
 * Evento que C-05 publica cuando se reprograma un turno.
 */
public record TurnoReprogramadoEvent(TurnoNotificable turno, LocalDateTime fechaHoraAnterior) {
}
