package com.consultorio.admin.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BloqueoResponse(
    UUID id,
    UUID profesionalId,
    UUID sillonBoxId,
    LocalDateTime fechaHoraInicio,
    LocalDateTime fechaHoraFin,
    String motivo,
    Boolean activo
) {
}
