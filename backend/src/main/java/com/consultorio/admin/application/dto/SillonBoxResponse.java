package com.consultorio.admin.application.dto;

import java.util.UUID;

public record SillonBoxResponse(UUID id, String nombre, Boolean activo) {
}
