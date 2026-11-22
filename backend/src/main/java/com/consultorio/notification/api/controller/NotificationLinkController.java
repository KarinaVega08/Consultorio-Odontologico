package com.consultorio.notification.api.controller;

import com.consultorio.notification.application.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Enlaces únicos públicos de confirmación y cancelación (RN-NT-02).
 * No requieren autenticación: el token es el mecanismo de autorización.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationLinkController {

    private final NotificacionService service;

    @GetMapping("/confirm")
    public String confirmarAsistencia(@RequestParam String token) {
        return service.confirmarAsistencia(token);
    }

    @GetMapping("/cancel")
    public String cancelarTurno(@RequestParam String token) {
        return service.cancelarTurnoConToken(token);
    }
}
