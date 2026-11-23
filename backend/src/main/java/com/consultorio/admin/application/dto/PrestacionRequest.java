package com.consultorio.admin.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrestacionRequest(
    @NotBlank(message = "El nombre de la prestación es obligatorio")
    String nombre,

    @NotNull(message = "La duración en minutos es obligatoria")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    Integer duracionMinutos,

    String descripcion
) {
}
