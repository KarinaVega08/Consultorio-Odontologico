package com.consultorio.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SillonBoxRequest(
    @NotBlank(message = "El nombre del sillón/box es obligatorio")
    String nombre
) {
}
