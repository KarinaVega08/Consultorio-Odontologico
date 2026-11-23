package com.consultorio.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Solicitud de reprogramación: solo cambia el inicio; el fin se recalcula desde la prestación.
 */
public record RescheduleAppointmentRequest(
    @NotNull(message = "La nueva fecha y hora de inicio es obligatoria")
    @FutureOrPresent(message = "La nueva fecha y hora de inicio no puede ser pasada") LocalDateTime fechaHoraInicio
) {
}
