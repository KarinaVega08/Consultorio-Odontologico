package com.consultorio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para autenticación de paciente con DNI y contraseña.
 */
public record LoginRequest(
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos")
        String dni,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
