package com.consultorio.notification.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Antelación de los recordatorios, configurable por el Administrador (IN-02).
 */
public record NotificationSettingsRequest(
    @NotNull(message = "La antelación es obligatoria")
    @Min(value = 1, message = "La antelación debe ser mayor a 0")
    Integer horasAntelacion
) {
}
