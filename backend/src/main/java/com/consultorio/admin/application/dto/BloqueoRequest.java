package com.consultorio.admin.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BloqueoRequest(
    UUID profesionalId,
    UUID sillonBoxId,

    @NotNull(message = "La fecha/hora de inicio es obligatoria")
    LocalDateTime fechaHoraInicio,

    @NotNull(message = "La fecha/hora de fin es obligatoria")
    LocalDateTime fechaHoraFin,

    String motivo
) {
}
