package com.consultorio.api.controller;

import com.consultorio.application.dto.AppointmentQuery;
import com.consultorio.application.dto.AppointmentResponse;
import com.consultorio.application.dto.CreateAppointmentRequest;
import com.consultorio.application.dto.RescheduleAppointmentRequest;
import com.consultorio.application.service.TokenService;
import com.consultorio.application.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * API REST de gestión de turnos (US-009 a US-012).
 * Nunca expone la entidad JPA: solo DTOs de respuesta.
 */
@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA', 'ODONTOLOGO', 'ADMINISTRADOR')")
@RequiredArgsConstructor
public class AppointmentController {

    private final TurnoService turnoService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> crear(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal TokenService.AuthenticatedUser usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(request, usuario));
    }

    @PutMapping("/{id}")
    public AppointmentResponse reprogramar(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            @AuthenticationPrincipal TokenService.AuthenticatedUser usuario) {
        return turnoService.reprogramar(id, request, usuario);
    }

    @DeleteMapping("/{id}")
    public AppointmentResponse cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal TokenService.AuthenticatedUser usuario) {
        return turnoService.cancelar(id, usuario);
    }

    @GetMapping
    public List<AppointmentResponse> listar(
            @RequestParam(required = false) UUID profesionalId,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) com.consultorio.domain.model.EstadoTurno estado,
            @AuthenticationPrincipal TokenService.AuthenticatedUser usuario) {
        return turnoService.listar(new AppointmentQuery(profesionalId, fecha, estado), usuario);
    }
}
