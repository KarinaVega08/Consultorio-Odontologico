package com.consultorio.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfesionalRequest(
    @NotBlank(message = "El nombre del profesional es obligatorio")
    String nombreCompleto,
    String matricula
) {
}
