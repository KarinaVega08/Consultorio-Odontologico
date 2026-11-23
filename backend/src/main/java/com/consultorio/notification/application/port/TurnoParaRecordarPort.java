package com.consultorio.notification.application.port;

import com.consultorio.notification.domain.model.TurnoNotificable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consulta de turnos a recordar. C-05 debe aportar la implementación real;
 * hasta entonces existe un bean no-op en NotificationIntegrationConfig.
 */
public interface TurnoParaRecordarPort {

    List<TurnoNotificable> findTurnosEnVentana(LocalDateTime desde, LocalDateTime hasta);
}
