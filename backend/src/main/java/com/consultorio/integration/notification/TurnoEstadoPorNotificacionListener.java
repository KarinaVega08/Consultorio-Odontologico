package com.consultorio.integration.notification;

import com.consultorio.application.service.TurnoService;
import com.consultorio.notification.application.event.AsistenciaConfirmadaEvent;
import com.consultorio.notification.application.event.TurnoCanceladoPorPacienteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consume los eventos de los enlaces únicos (C-06) y actualiza el estado del turno
 * reutilizando la lógica de C-05. Idempotente: los enlaces son públicos y no deben
 * romperse aunque el turno ya esté cancelado/confirmado o no exista.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnoEstadoPorNotificacionListener {

    private final TurnoService turnoService;

    @EventListener
    public void onTurnoCanceladoPorPaciente(TurnoCanceladoPorPacienteEvent event) {
        log.debug("Cancelación por enlace recibida para el turno {}", event.turnoId());
        turnoService.cancelarDesdeEnlace(event.turnoId(), event.fechaCancelacion());
    }

    @EventListener
    public void onAsistenciaConfirmada(AsistenciaConfirmadaEvent event) {
        log.debug("Confirmación por enlace recibida para el turno {}", event.turnoId());
        turnoService.confirmarDesdeEnlace(event.turnoId(), event.fechaConfirmacion());
    }
}
