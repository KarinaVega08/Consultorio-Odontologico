package com.consultorio.application.service;

import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.application.dto.AvailabilityRequest;
import com.consultorio.application.dto.AvailabilityResponse;
import com.consultorio.application.dto.AvailabilitySlotResponse;
import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.availability.AvailabilityCalculator;
import com.consultorio.domain.availability.AvailabilitySlot;
import com.consultorio.domain.availability.Block;
import com.consultorio.domain.availability.TipoBloqueo;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import com.consultorio.domain.availability.WorkWindow;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.infrastructure.config.AvailabilityProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Consulta de disponibilidad: arma los DTOs del calculador a partir de la
 * configuración de C-03 y de los turnos provistos por C-05.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    private final ProfesionalRepository profesionalRepository;
    private final PrestacionRepository prestacionRepository;
    private final SillonBoxRepository sillonBoxRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final BloqueoRepository bloqueoRepository;
    private final TurnoDisponibilidadPort turnoDisponibilidadPort;
    private final AvailabilityCalculator availabilityCalculator;
    private final AvailabilityProperties properties;

    public AvailabilityResponse consultar(AvailabilityRequest request) {
        Profesional profesional = profesionalRepository.findById(request.profesionalId())
            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profesional no encontrado: " + request.profesionalId()));

        Prestacion prestacion = prestacionRepository.findById(request.prestacionId())
            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException(
                "Prestación no encontrada: " + request.prestacionId()));

        if (request.sillonBoxId() != null) {
            sillonBoxRepository.findById(request.sillonBoxId())
                .filter(s -> Boolean.TRUE.equals(s.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Sillón/box no encontrado: " + request.sillonBoxId()));
        }

        List<WorkWindow> ventanas = horarioAtencionRepository.findByProfesionalId(profesional.getId())
            .stream()
            .filter(h -> Boolean.TRUE.equals(h.getActivo()))
            .filter(h -> Objects.equals(h.getDiaSemana(), request.fecha().getDayOfWeek().getValue()))
            .map(this::toWorkWindow)
            .toList();

        List<Block> bloqueos = bloqueosAplicables(profesional.getId(), request.sillonBoxId());

        List<Appointment> turnos = turnoDisponibilidadPort.findTurnosOcupados(
            profesional.getId(), request.fecha(), request.sillonBoxId());

        List<AvailabilitySlot> franjas = availabilityCalculator.calculate(
            request.fecha(), ventanas, bloqueos, turnos,
            prestacion.getDuracionMinutos(), properties.gridMinutes());

        log.info("Disponibilidad calculada: profesional={} fecha={} franjas={}",
            profesional.getId(), request.fecha(), franjas.size());

        return new AvailabilityResponse(
            profesional.getId(),
            request.fecha(),
            prestacion.getDuracionMinutos(),
            franjas.stream().map(s -> new AvailabilitySlotResponse(s.inicio(), s.fin())).toList());
    }

    private WorkWindow toWorkWindow(HorarioAtencion horario) {
        return new WorkWindow(horario.getHoraInicio(), horario.getHoraFin());
    }

    private List<Block> bloqueosAplicables(UUID profesionalId, UUID sillonBoxId) {
        Map<Object, Bloqueo> vistos = new LinkedHashMap<>();
        bloqueoRepository.findByProfesionalId(profesionalId)
            .forEach(bloqueo -> vistos.putIfAbsent(clave(bloqueo), bloqueo));
        if (sillonBoxId != null) {
            bloqueoRepository.findBySillonBoxId(sillonBoxId)
                .forEach(bloqueo -> vistos.putIfAbsent(clave(bloqueo), bloqueo));
        }

        List<Block> bloqueos = new ArrayList<>();
        for (Bloqueo bloqueo : vistos.values()) {
            if (!Boolean.TRUE.equals(bloqueo.getActivo())) {
                continue;
            }
            TipoBloqueo tipo = bloqueo.getProfesional() != null
                ? TipoBloqueo.PROFESIONAL : TipoBloqueo.SILLON;
            bloqueos.add(new Block(
                bloqueo.getFechaHoraInicio(), bloqueo.getFechaHoraFin(), tipo));
        }
        return bloqueos;
    }

    private Object clave(Bloqueo bloqueo) {
        return bloqueo.getId() != null ? bloqueo.getId() : bloqueo;
    }
}
