package com.consultorio.integration.notification;

import com.consultorio.domain.model.Turno;
import com.consultorio.notification.application.event.TurnoCanceladoEvent;
import com.consultorio.notification.application.event.TurnoCreadoEvent;
import com.consultorio.notification.application.event.TurnoReprogramadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publica los eventos de C-05 hacia C-06. Se llama al final de cada operación
 * de {@code TurnoService}, después de persistir y dentro de la transacción.
 * Sin correo de recuperación no publica: evita disparar el fallback SMTP con
 * destinatario nulo (decisión registrada en el spec).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnosNotificacionPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final TurnoNotificableMapper mapper;

    public void publicarCreado(Turno turno) {
        if (!tieneCorreo(turno)) {
            return;
        }
        eventPublisher.publishEvent(new TurnoCreadoEvent(mapper.toNotificable(turno)));
        log.debug("Evento TurnoCreado publicado para el turno {}", turno.getId());
    }

    public void publicarReprogramado(Turno turno, LocalDateTime fechaHoraAnterior) {
        if (!tieneCorreo(turno)) {
            return;
        }
        eventPublisher.publishEvent(new TurnoReprogramadoEvent(mapper.toNotificable(turno), fechaHoraAnterior));
        log.debug("Evento TurnoReprogramado publicado para el turno {}", turno.getId());
    }

    public void publicarCancelado(Turno turno) {
        if (!tieneCorreo(turno)) {
            return;
        }
        eventPublisher.publishEvent(new TurnoCanceladoEvent(mapper.toNotificable(turno)));
        log.debug("Evento TurnoCancelado publicado para el turno {}", turno.getId());
    }

    private boolean tieneCorreo(Turno turno) {
        String email = turno.getPaciente().getEmailRecuperacion();
        return email != null && !email.isBlank();
    }
}
