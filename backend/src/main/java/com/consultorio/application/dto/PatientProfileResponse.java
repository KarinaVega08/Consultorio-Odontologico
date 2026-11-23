package com.consultorio.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida con los datos públicos del perfil del paciente.
 */
public record PatientProfileResponse(
        UUID id,
        String nombreCompleto,
        String dni,
        String emailRecuperacion,
        boolean requiereVincularGmail,
        Instant fechaRegistro
) {
}
