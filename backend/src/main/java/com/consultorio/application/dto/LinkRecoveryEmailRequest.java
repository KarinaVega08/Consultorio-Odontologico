package com.consultorio.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para vincular por única vez el correo Gmail de recuperación.
 */
public record LinkRecoveryEmailRequest(
        @NotBlank(message = "El correo Gmail es obligatorio")
        @Email(message = "El formato del correo electrónico es inválido")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "El correo de recuperación debe ser una dirección @gmail.com válida")
        String email
) {
}
