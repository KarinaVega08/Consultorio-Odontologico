package com.consultorio.admin.application.dto;

import java.time.LocalTime;
import java.util.UUID;

public record HorarioAtencionResponse(
    UUID id,
    UUID profesionalId,
    Integer diaSemana,
    LocalTime horaInicio,
    LocalTime horaFin,
    Boolean activo
) {
}
