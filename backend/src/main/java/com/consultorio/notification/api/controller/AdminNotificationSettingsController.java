package com.consultorio.notification.api.controller;

import com.consultorio.notification.application.dto.NotificationSettingsRequest;
import com.consultorio.notification.application.dto.NotificationSettingsResponse;
import com.consultorio.notification.application.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuración de la antelación de recordatorios (IN-02). Solo Administrador.
 */
@RestController
@RequestMapping("/api/admin/notification-settings")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminNotificationSettingsController {

    private final NotificacionService service;

    @GetMapping
    public NotificationSettingsResponse obtener() {
        return new NotificationSettingsResponse(service.obtenerHorasAntelacion());
    }

    @PutMapping
    public NotificationSettingsResponse actualizar(@Valid @RequestBody NotificationSettingsRequest request) {
        return new NotificationSettingsResponse(service.actualizarHorasAntelacion(request.horasAntelacion()));
    }
}
