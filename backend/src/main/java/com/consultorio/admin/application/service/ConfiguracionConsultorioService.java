package com.consultorio.admin.application.service;

import com.consultorio.admin.application.dto.BloqueoRequest;
import com.consultorio.admin.application.dto.BloqueoResponse;
import com.consultorio.admin.application.dto.HorarioAtencionRequest;
import com.consultorio.admin.application.dto.HorarioAtencionResponse;
import com.consultorio.admin.application.dto.PrestacionRequest;
import com.consultorio.admin.application.dto.PrestacionResponse;
import com.consultorio.admin.application.dto.ProfesionalRequest;
import com.consultorio.admin.application.dto.ProfesionalResponse;
import com.consultorio.admin.application.dto.SillonBoxRequest;
import com.consultorio.admin.application.dto.SillonBoxResponse;
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
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Configuración administrativa del consultorio (RN-TU-03, RN-TU-05).
 * Todas las bajas son lógicas: se desactiva {@code activo}, nunca se borra la fila.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConfiguracionConsultorioService {

    private final ProfesionalRepository profesionalRepository;
    private final SillonBoxRepository sillonBoxRepository;
    private final PrestacionRepository prestacionRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final BloqueoRepository bloqueoRepository;

    // ---------------------------------------------------------------- Profesionales

    public ProfesionalResponse crearProfesional(ProfesionalRequest request) {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto(request.nombreCompleto());
        profesional.setMatricula(request.matricula());
        Profesional guardado = profesionalRepository.save(profesional);
        log.info("Profesional creado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    public ProfesionalResponse actualizarProfesional(UUID id, ProfesionalRequest request) {
        Profesional profesional = buscarProfesional(id);
        profesional.setNombreCompleto(request.nombreCompleto());
        profesional.setMatricula(request.matricula());
        return toResponse(profesionalRepository.save(profesional));
    }

    public List<ProfesionalResponse> listarProfesionales() {
        return profesionalRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public ProfesionalResponse activarProfesional(UUID id, boolean activo) {
        Profesional profesional = buscarProfesional(id);
        profesional.setActivo(activo);
        log.info("Profesional {} activo={}", id, activo);
        return toResponse(profesionalRepository.save(profesional));
    }

    private Profesional buscarProfesional(UUID id) {
        return profesionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado: " + id));
    }

    private ProfesionalResponse toResponse(Profesional profesional) {
        return new ProfesionalResponse(
            profesional.getId(),
            profesional.getNombreCompleto(),
            profesional.getMatricula(),
            profesional.getActivo()
        );
    }

    // ------------------------------------------------------------- Sillones/boxes

    public SillonBoxResponse crearSillonBox(SillonBoxRequest request) {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setNombre(request.nombre());
        SillonBox guardado = sillonBoxRepository.save(sillonBox);
        log.info("Sillón/box creado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    public SillonBoxResponse actualizarSillonBox(UUID id, SillonBoxRequest request) {
        SillonBox sillonBox = buscarSillonBox(id);
        sillonBox.setNombre(request.nombre());
        return toResponse(sillonBoxRepository.save(sillonBox));
    }

    public List<SillonBoxResponse> listarSillonesBoxes() {
        return sillonBoxRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public SillonBoxResponse activarSillonBox(UUID id, boolean activo) {
        SillonBox sillonBox = buscarSillonBox(id);
        sillonBox.setActivo(activo);
        return toResponse(sillonBoxRepository.save(sillonBox));
    }

    private SillonBox buscarSillonBox(UUID id) {
        return sillonBoxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sillón/box no encontrado: " + id));
    }

    private SillonBoxResponse toResponse(SillonBox sillonBox) {
        return new SillonBoxResponse(sillonBox.getId(), sillonBox.getNombre(), sillonBox.getActivo());
    }

    // --------------------------------------------------------------- Prestaciones

    public PrestacionResponse crearPrestacion(PrestacionRequest request) {
        validarDuracion(request.duracionMinutos());
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre(request.nombre());
        prestacion.setDuracionMinutos(request.duracionMinutos());
        prestacion.setDescripcion(request.descripcion());
        Prestacion guardada = prestacionRepository.save(prestacion);
        log.info("Prestación creada con id {} y duración {} min", guardada.getId(), guardada.getDuracionMinutos());
        return toResponse(guardada);
    }

    public PrestacionResponse actualizarPrestacion(UUID id, PrestacionRequest request) {
        validarDuracion(request.duracionMinutos());
        Prestacion prestacion = buscarPrestacion(id);
        prestacion.setNombre(request.nombre());
        prestacion.setDuracionMinutos(request.duracionMinutos());
        prestacion.setDescripcion(request.descripcion());
        return toResponse(prestacionRepository.save(prestacion));
    }

    public List<PrestacionResponse> listarPrestaciones() {
        return prestacionRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public PrestacionResponse activarPrestacion(UUID id, boolean activo) {
        Prestacion prestacion = buscarPrestacion(id);
        prestacion.setActivo(activo);
        return toResponse(prestacionRepository.save(prestacion));
    }

    private void validarDuracion(Integer duracionMinutos) {
        if (duracionMinutos == null || duracionMinutos <= 0) {
            throw new BusinessException("La duración de la prestación debe ser mayor a 0 minutos");
        }
    }

    private Prestacion buscarPrestacion(UUID id) {
        return prestacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prestación no encontrada: " + id));
    }

    private PrestacionResponse toResponse(Prestacion prestacion) {
        return new PrestacionResponse(
            prestacion.getId(),
            prestacion.getNombre(),
            prestacion.getDuracionMinutos(),
            prestacion.getDescripcion(),
            prestacion.getActivo()
        );
    }

    // ------------------------------------------------------------ Horarios de atención

    public HorarioAtencionResponse crearHorario(HorarioAtencionRequest request) {
        validarHorario(request);
        Profesional profesional = buscarProfesional(request.profesionalId());
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        HorarioAtencion guardado = horarioAtencionRepository.save(horario);
        log.info("Horario creado con id {} para profesional {}", guardado.getId(), profesional.getId());
        return toResponse(guardado);
    }

    public List<HorarioAtencionResponse> listarHorarios() {
        return horarioAtencionRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public List<HorarioAtencionResponse> listarHorariosPorProfesional(UUID profesionalId) {
        return horarioAtencionRepository.findByProfesionalId(profesionalId).stream().map(this::toResponse).toList();
    }

    public HorarioAtencionResponse actualizarHorario(UUID id, HorarioAtencionRequest request) {
        validarHorario(request);
        HorarioAtencion horario = horarioAtencionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + id));
        horario.setProfesional(buscarProfesional(request.profesionalId()));
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        return toResponse(horarioAtencionRepository.save(horario));
    }

    public HorarioAtencionResponse activarHorario(UUID id, boolean activo) {
        HorarioAtencion horario = horarioAtencionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + id));
        horario.setActivo(activo);
        return toResponse(horarioAtencionRepository.save(horario));
    }

    private void validarHorario(HorarioAtencionRequest request) {
        if (request.diaSemana() == null || request.diaSemana() < 1 || request.diaSemana() > 7) {
            throw new BusinessException("El día de semana debe estar entre 1 (lunes) y 7 (domingo)");
        }
        if (request.horaInicio() == null || request.horaFin() == null
            || !request.horaFin().isAfter(request.horaInicio())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private HorarioAtencionResponse toResponse(HorarioAtencion horario) {
        return new HorarioAtencionResponse(
            horario.getId(),
            horario.getProfesional().getId(),
            horario.getDiaSemana(),
            horario.getHoraInicio(),
            horario.getHoraFin(),
            horario.getActivo()
        );
    }

    // --------------------------------------------------------------------- Bloqueos

    public BloqueoResponse crearBloqueo(BloqueoRequest request) {
        validarBloqueo(request);
        Bloqueo bloqueo = new Bloqueo();
        if (request.profesionalId() != null) {
            bloqueo.setProfesional(buscarProfesional(request.profesionalId()));
        }
        if (request.sillonBoxId() != null) {
            bloqueo.setSillonBox(buscarSillonBox(request.sillonBoxId()));
        }
        bloqueo.setFechaHoraInicio(request.fechaHoraInicio());
        bloqueo.setFechaHoraFin(request.fechaHoraFin());
        bloqueo.setMotivo(request.motivo());
        Bloqueo guardado = bloqueoRepository.save(bloqueo);
        log.info("Bloqueo creado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    public List<BloqueoResponse> listarBloqueos() {
        return bloqueoRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public List<BloqueoResponse> listarBloqueosPorProfesional(UUID profesionalId) {
        return bloqueoRepository.findByProfesionalId(profesionalId).stream().map(this::toResponse).toList();
    }

    public List<BloqueoResponse> listarBloqueosPorSillonBox(UUID sillonBoxId) {
        return bloqueoRepository.findBySillonBoxId(sillonBoxId).stream().map(this::toResponse).toList();
    }

    public BloqueoResponse actualizarBloqueo(UUID id, BloqueoRequest request) {
        validarBloqueo(request);
        Bloqueo bloqueo = bloqueoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bloqueo no encontrado: " + id));
        bloqueo.setProfesional(request.profesionalId() == null ? null : buscarProfesional(request.profesionalId()));
        bloqueo.setSillonBox(request.sillonBoxId() == null ? null : buscarSillonBox(request.sillonBoxId()));
        bloqueo.setFechaHoraInicio(request.fechaHoraInicio());
        bloqueo.setFechaHoraFin(request.fechaHoraFin());
        bloqueo.setMotivo(request.motivo());
        return toResponse(bloqueoRepository.save(bloqueo));
    }

    public BloqueoResponse activarBloqueo(UUID id, boolean activo) {
        Bloqueo bloqueo = bloqueoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bloqueo no encontrado: " + id));
        bloqueo.setActivo(activo);
        return toResponse(bloqueoRepository.save(bloqueo));
    }

    private void validarBloqueo(BloqueoRequest request) {
        if (request.profesionalId() == null && request.sillonBoxId() == null) {
            throw new BusinessException("El bloqueo debe indicar un profesional o un sillón/box");
        }
        if (request.fechaHoraInicio() == null || request.fechaHoraFin() == null
            || !request.fechaHoraFin().isAfter(request.fechaHoraInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private BloqueoResponse toResponse(Bloqueo bloqueo) {
        return new BloqueoResponse(
            bloqueo.getId(),
            bloqueo.getProfesional() == null ? null : bloqueo.getProfesional().getId(),
            bloqueo.getSillonBox() == null ? null : bloqueo.getSillonBox().getId(),
            bloqueo.getFechaHoraInicio(),
            bloqueo.getFechaHoraFin(),
            bloqueo.getMotivo(),
            bloqueo.getActivo()
        );
    }
}
