package com.consultorio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el registro de un paciente.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 2, max = 150, message = "El nombre completo debe tener entre 2 y 150 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos")
        String dni,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password
) {
}
