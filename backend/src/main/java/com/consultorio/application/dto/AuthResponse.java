package com.consultorio.application.dto;

/**
 * DTO de salida para el login exitoso con token Bearer y perfil del paciente.
 */
public record AuthResponse(
        String token,
        String tokenType,
        PatientProfileResponse patient
) {
}
