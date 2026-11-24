package com.consultorio.integration.notification;

import com.consultorio.domain.repository.TurnoRepository;
import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import com.consultorio.notification.domain.model.TurnoNotificable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación real del puerto de recordatorios. Al existir este bean, el no-op
 * de {@code NotificationIntegrationConfig} se desactiva por su @ConditionalOnMissingBean.
 */
@Component
@RequiredArgsConstructor
public class TurnoRecordatorioAdapter implements TurnoParaRecordarPort {

    private final TurnoRepository turnoRepository;
    private final TurnoNotificableMapper mapper;

    @Override
    public List<TurnoNotificable> findTurnosEnVentana(LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepository.findRecordatoriosEnVentana(desde, hasta).stream()
            .map(mapper::toNotificable)
            .toList();
    }
}
