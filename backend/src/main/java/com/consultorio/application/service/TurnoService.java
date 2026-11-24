package com.consultorio.application.service;

import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.application.dto.AppointmentQuery;
import com.consultorio.application.dto.AppointmentResponse;
import com.consultorio.application.dto.CreateAppointmentRequest;
import com.consultorio.application.dto.RescheduleAppointmentRequest;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.domain.model.Turno;
import com.consultorio.domain.model.TurnoSolapamiento;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.TurnoRepository;
import com.consultorio.integration.notification.TurnosNotificacionPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Gestión transaccional de la agenda: creación, reprogramación, cancelación y consulta.
 * La validación anti-solapamiento usa la regla estricta {@code inicioA < finB && inicioB < finA}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    static final String SOLAPAMIENTO = "El profesional o el sillón ya tiene un turno en ese horario";
    static final String FUERA_DE_HORARIO = "El horario seleccionado no está dentro del horario de atención";
    static final String BLOQUEADO = "El horario seleccionado está bloqueado";
    static final String YA_CANCELADO = "El turno ya se encuentra cancelado";

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final SillonBoxRepository sillonBoxRepository;
    private final PrestacionRepository prestacionRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final BloqueoRepository bloqueoRepository;
    private final TurnosNotificacionPublisher notificacionPublisher;

    @Transactional
    public AppointmentResponse crear(CreateAppointmentRequest request,
                                     TokenService.AuthenticatedUser usuario) {
        validarPacienteAutorizado(request.pacienteId(), usuario);

        // Mismo orden de lock siempre: profesional y después sillón/box (D2, IN-04).
        Profesional profesional = profesionalRepository.findByIdForUpdate(request.profesionalId())
            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado: " + request.profesionalId()));
        SillonBox sillonBox = sillonBoxRepository.findByIdForUpdate(request.sillonBoxId())
            .filter(s -> Boolean.TRUE.equals(s.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException("Sillón/box no encontrado: " + request.sillonBoxId()));

        Paciente paciente = pacienteRepository.findById(request.pacienteId())
            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + request.pacienteId()));
        Prestacion prestacion = prestacionRepository.findById(request.prestacionId())
            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(() -> new ResourceNotFoundException("Prestación no encontrada: " + request.prestacionId()));

        LocalDateTime inicio = request.fechaHoraInicio();
        LocalDateTime fin = inicio.plusMinutes(prestacion.getDuracionMinutos());

        validarHorario(profesional, inicio, fin);
        validarBloqueos(profesional.getId(), sillonBox.getId(), inicio, fin);
        validarSolapes(profesional.getId(), sillonBox.getId(), inicio, fin, null);

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setSillonBox(sillonBox);
        turno.setPrestacion(prestacion);
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCreadoPor(usuario.dni());

        Turno guardado = turnoRepository.save(turno);
        log.info("Turno {} creado para profesional {} en {}", guardado.getId(), profesional.getId(), inicio);
        notificacionPublisher.publicarCreado(guardado);
        return toResponse(guardado);
    }

    @Transactional
    public AppointmentResponse reprogramar(UUID id, RescheduleAppointmentRequest request,
                                           TokenService.AuthenticatedUser usuario) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado: " + id));
        validarPacienteAutorizado(turno.getPaciente().getId(), usuario);

        Profesional profesional = profesionalRepository.findByIdForUpdate(turno.getProfesional().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado: " + turno.getProfesional().getId()));
        SillonBox sillonBox = sillonBoxRepository.findByIdForUpdate(turno.getSillonBox().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Sillón/box no encontrado: " + turno.getSillonBox().getId()));

        LocalDateTime inicio = request.fechaHoraInicio();
        LocalDateTime fin = inicio.plusMinutes(turno.getPrestacion().getDuracionMinutos());

        validarHorario(profesional, inicio, fin);
        validarBloqueos(profesional.getId(), sillonBox.getId(), inicio, fin);
        validarSolapes(profesional.getId(), sillonBox.getId(), inicio, fin, turno.getId());

        LocalDateTime fechaHoraAnterior = turno.getFechaHoraInicio();
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        Turno guardado = turnoRepository.save(turno);
        log.info("Turno {} reprogramado a {}", guardado.getId(), inicio);
        notificacionPublisher.publicarReprogramado(guardado, fechaHoraAnterior);
        return toResponse(guardado);
    }

    @Transactional
    public AppointmentResponse cancelar(UUID id, TokenService.AuthenticatedUser usuario) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado: " + id));
        validarPacienteAutorizado(turno.getPaciente().getId(), usuario);

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new BusinessException(YA_CANCELADO);
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        Turno guardado = turnoRepository.save(turno);
        log.info("Turno {} cancelado; hueco liberado", guardado.getId());
        notificacionPublisher.publicarCancelado(guardado);
        return toResponse(guardado);
    }

    /** Consumo del enlace único de cancelación: idempotente, nunca lanza excepción al público. */
    @Transactional
    public void cancelarDesdeEnlace(UUID turnoId, LocalDateTime fechaCancelacion) {
        Turno turno = turnoRepository.findById(turnoId).orElse(null);
        if (turno == null) {
            log.info("Enlace de cancelación: turno {} no existe; se ignora", turnoId);
            return;
        }
        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            log.info("Enlace de cancelación: turno {} ya estaba cancelado", turnoId);
            return;
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        turnoRepository.save(turno);
        log.info("Turno {} cancelado desde enlace único en {}", turnoId, fechaCancelacion);
    }

    /** Consumo del enlace único de confirmación: no reactiva turnos cancelados. */
    @Transactional
    public void confirmarDesdeEnlace(UUID turnoId, LocalDateTime fechaConfirmacion) {
        Turno turno = turnoRepository.findById(turnoId).orElse(null);
        if (turno == null) {
            log.info("Enlace de confirmación: turno {} no existe; se ignora", turnoId);
            return;
        }
        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            log.info("Enlace de confirmación: turno {} está cancelado; no se reactiva", turnoId);
            return;
        }
        if (turno.getEstado() == EstadoTurno.CONFIRMADO) {
            log.info("Enlace de confirmación: turno {} ya estaba confirmado", turnoId);
            return;
        }
        turno.setEstado(EstadoTurno.CONFIRMADO);
        turnoRepository.save(turno);
        log.info("Turno {} confirmado desde enlace único en {}", turnoId, fechaConfirmacion);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listar(AppointmentQuery query, TokenService.AuthenticatedUser usuario) {
        LocalDateTime desde = query.fecha() == null ? null : query.fecha().atStartOfDay();
        LocalDateTime hasta = query.fecha() == null ? null : query.fecha().atTime(LocalTime.MAX);

        return turnoRepository.buscarConFiltros(query.profesionalId(), desde, hasta, query.estado()).stream()
            .filter(turno -> usuario.rol() != RolUsuario.PACIENTE
                || turno.getPaciente().getId().equals(usuario.userId()))
            .map(TurnoService::toResponse)
            .toList();
    }

    private void validarPacienteAutorizado(UUID pacienteId, TokenService.AuthenticatedUser usuario) {
        if (usuario.rol() == RolUsuario.PACIENTE && !pacienteId.equals(usuario.userId())) {
            throw new AccessDeniedException("El paciente solo puede operar sobre sus propios turnos");
        }
    }

    private void validarHorario(Profesional profesional, LocalDateTime inicio, LocalDateTime fin) {
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        int diaSemana = inicio.getDayOfWeek().getValue();

        boolean cabe = horarioAtencionRepository.findByProfesionalId(profesional.getId()).stream()
            .filter(h -> Boolean.TRUE.equals(h.getActivo()))
            .filter(h -> h.getDiaSemana() == diaSemana)
            .anyMatch(h -> !horaInicio.isBefore(h.getHoraInicio()) && !horaFin.isAfter(h.getHoraFin()));

        if (!cabe) {
            throw new BusinessException(FUERA_DE_HORARIO);
        }
    }

    private void validarBloqueos(UUID profesionalId, UUID sillonBoxId, LocalDateTime inicio, LocalDateTime fin) {
        List<Bloqueo> bloqueos = new java.util.ArrayList<>();
        bloqueos.addAll(bloqueoRepository.findByProfesionalId(profesionalId));
        bloqueos.addAll(bloqueoRepository.findBySillonBoxId(sillonBoxId));

        boolean choca = bloqueos.stream()
            .filter(b -> Boolean.TRUE.equals(b.getActivo()))
            .anyMatch(b -> TurnoSolapamiento.seSolapan(inicio, fin, b.getFechaHoraInicio(), b.getFechaHoraFin()));

        if (choca) {
            throw new BusinessException(BLOQUEADO);
        }
    }

    private void validarSolapes(UUID profesionalId, UUID sillonBoxId, LocalDateTime inicio,
                                LocalDateTime fin, UUID turnoId) {
        boolean solape = !turnoRepository.findSolapesPorProfesional(profesionalId, inicio, fin, turnoId).isEmpty()
            || !turnoRepository.findSolapesPorSillon(sillonBoxId, inicio, fin, turnoId).isEmpty();
        if (solape) {
            throw new BusinessException(SOLAPAMIENTO);
        }
    }

    static AppointmentResponse toResponse(Turno turno) {
        return new AppointmentResponse(
            turno.getId(),
            turno.getPaciente().getId(),
            turno.getProfesional().getId(),
            turno.getSillonBox().getId(),
            turno.getPrestacion().getId(),
            turno.getFechaHoraInicio(),
            turno.getFechaHoraFin(),
            turno.getEstado(),
            turno.getCreadoPor(),
            turno.getFechaCreacion()
        );
    }
}
