package com.consultorio.infrastructure.persistence;

import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import com.consultorio.domain.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación real del puerto de disponibilidad que C-04 dejó preparado.
 * El fallback {@code NoOpTurnoDisponibilidadPort} se retrae automáticamente
 * porque está anotado con {@code @ConditionalOnMissingBean}.
 */
@Component
@RequiredArgsConstructor
public class TurnoDisponibilidadAdapter implements TurnoDisponibilidadPort {

    private final TurnoRepository turnoRepository;

    @Override
    public List<Appointment> findTurnosOcupados(UUID profesionalId, LocalDate fecha, UUID sillonBoxId) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(LocalTime.MAX);

        return turnoRepository.buscarOcupados(profesionalId, desde, hasta, sillonBoxId).stream()
            .map(turno -> new Appointment(
                turno.getFechaHoraInicio(), turno.getFechaHoraFin(), turno.getEstado()))
            .toList();
    }
}
