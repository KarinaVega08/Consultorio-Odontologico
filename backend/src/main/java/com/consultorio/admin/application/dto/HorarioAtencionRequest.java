package com.consultorio.admin.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record HorarioAtencionRequest(
    @NotNull(message = "El profesional es obligatorio")
    UUID profesionalId,

    @NotNull(message = "El día de semana es obligatorio")
    @Min(value = 1, message = "El día debe estar entre 1 y 7")
    @Max(value = 7, message = "El día debe estar entre 1 y 7")
    Integer diaSemana,

    @NotNull(message = "La hora de inicio es obligatoria")
    LocalTime horaInicio,

    @NotNull(message = "La hora de fin es obligatoria")
    LocalTime horaFin
) {
}
