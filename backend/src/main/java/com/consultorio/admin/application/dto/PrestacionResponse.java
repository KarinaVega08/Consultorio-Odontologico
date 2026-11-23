package com.consultorio.admin.application.dto;

import java.util.UUID;

public record PrestacionResponse(
    UUID id,
    String nombre,
    Integer duracionMinutos,
    String descripcion,
    Boolean activo
) {
}
