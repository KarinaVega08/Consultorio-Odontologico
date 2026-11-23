package com.consultorio.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Solicitud de creación de turno. La hora de fin se calcula siempre en el servidor.
 */
public record CreateAppointmentRequest(
    @NotNull(message = "El paciente es obligatorio") UUID pacienteId,
    @NotNull(message = "El profesional es obligatorio") UUID profesionalId,
    @NotNull(message = "El sillón/box es obligatorio") UUID sillonBoxId,
    @NotNull(message = "La prestación es obligatoria") UUID prestacionId,
    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha y hora de inicio no puede ser pasada") LocalDateTime fechaHoraInicio
) {
}
