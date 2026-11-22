package com.consultorio.admin.application.dto;

import java.util.UUID;

public record ProfesionalResponse(UUID id, String nombreCompleto, String matricula, Boolean activo) {
}
