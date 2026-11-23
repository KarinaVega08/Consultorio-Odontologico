package com.consultorio.application.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Parámetros de la consulta {@code GET /api/availability}.
 * El sillón/box es opcional: si se omite, la disponibilidad se calcula para el profesional.
 */
public record AvailabilityRequest(

    @NotNull(message = "profesionalId es obligatorio")
    UUID profesionalId,

    @NotNull(message = "fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fecha,

    @NotNull(message = "prestacionId es obligatorio")
    UUID prestacionId,

    UUID sillonBoxId
) {
}
