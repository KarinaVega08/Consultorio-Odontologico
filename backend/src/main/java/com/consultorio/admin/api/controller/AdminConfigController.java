package com.consultorio.admin.api.controller;

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
import com.consultorio.admin.application.service.ConfiguracionConsultorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Configuración del consultorio. Solo Administrador (RN-TU-05).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminConfigController {

    private final ConfiguracionConsultorioService service;

    @PostMapping("/professionals")
    public ResponseEntity<ProfesionalResponse> crearProfesional(@Valid @RequestBody ProfesionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearProfesional(request));
    }

    @GetMapping("/professionals")
    public List<ProfesionalResponse> listarProfesionales() {
        return service.listarProfesionales();
    }

    @PutMapping("/professionals/{id}")
    public ProfesionalResponse actualizarProfesional(@PathVariable UUID id,
                                                     @Valid @RequestBody ProfesionalRequest request) {
        return service.actualizarProfesional(id, request);
    }

    @PatchMapping("/professionals/{id}/active")
    public ProfesionalResponse activarProfesional(@PathVariable UUID id, @RequestParam boolean activo) {
        return service.activarProfesional(id, activo);
    }

    @PostMapping("/consulting-rooms")
    public ResponseEntity<SillonBoxResponse> crearSillonBox(@Valid @RequestBody SillonBoxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearSillonBox(request));
    }

    @GetMapping("/consulting-rooms")
    public List<SillonBoxResponse> listarSillonesBoxes() {
        return service.listarSillonesBoxes();
    }

    @PutMapping("/consulting-rooms/{id}")
    public SillonBoxResponse actualizarSillonBox(@PathVariable UUID id,
                                                 @Valid @RequestBody SillonBoxRequest request) {
        return service.actualizarSillonBox(id, request);
    }

    @PatchMapping("/consulting-rooms/{id}/active")
    public SillonBoxResponse activarSillonBox(@PathVariable UUID id, @RequestParam boolean activo) {
        return service.activarSillonBox(id, activo);
    }

    @PostMapping("/services")
    public ResponseEntity<PrestacionResponse> crearPrestacion(@Valid @RequestBody PrestacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPrestacion(request));
    }

    @GetMapping("/services")
    public List<PrestacionResponse> listarPrestaciones() {
        return service.listarPrestaciones();
    }

    @PutMapping("/services/{id}")
    public PrestacionResponse actualizarPrestacion(@PathVariable UUID id,
                                                   @Valid @RequestBody PrestacionRequest request) {
        return service.actualizarPrestacion(id, request);
    }

    @PatchMapping("/services/{id}/active")
    public PrestacionResponse activarPrestacion(@PathVariable UUID id, @RequestParam boolean activo) {
        return service.activarPrestacion(id, activo);
    }

    @PostMapping("/schedules")
    public ResponseEntity<HorarioAtencionResponse> crearHorario(@Valid @RequestBody HorarioAtencionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearHorario(request));
    }

    @GetMapping("/schedules")
    public List<HorarioAtencionResponse> listarHorarios(
        @RequestParam(required = false) UUID profesionalId
    ) {
        return profesionalId == null ? service.listarHorarios() : service.listarHorariosPorProfesional(profesionalId);
    }

    @PutMapping("/schedules/{id}")
    public HorarioAtencionResponse actualizarHorario(@PathVariable UUID id,
                                                     @Valid @RequestBody HorarioAtencionRequest request) {
        return service.actualizarHorario(id, request);
    }

    @PatchMapping("/schedules/{id}/active")
    public HorarioAtencionResponse activarHorario(@PathVariable UUID id, @RequestParam boolean activo) {
        return service.activarHorario(id, activo);
    }

    @PostMapping("/blockers")
    public ResponseEntity<BloqueoResponse> crearBloqueo(@Valid @RequestBody BloqueoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBloqueo(request));
    }

    @GetMapping("/blockers")
    public List<BloqueoResponse> listarBloqueos(
        @RequestParam(required = false) UUID profesionalId,
        @RequestParam(required = false) UUID sillonBoxId
    ) {
        if (profesionalId != null) {
            return service.listarBloqueosPorProfesional(profesionalId);
        }
        if (sillonBoxId != null) {
            return service.listarBloqueosPorSillonBox(sillonBoxId);
        }
        return service.listarBloqueos();
    }

    @PutMapping("/blockers/{id}")
    public BloqueoResponse actualizarBloqueo(@PathVariable UUID id,
                                             @Valid @RequestBody BloqueoRequest request) {
        return service.actualizarBloqueo(id, request);
    }

    @PatchMapping("/blockers/{id}/active")
    public BloqueoResponse activarBloqueo(@PathVariable UUID id, @RequestParam boolean activo) {
        return service.activarBloqueo(id, activo);
    }
}
