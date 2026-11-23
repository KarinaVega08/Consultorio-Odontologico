package com.consultorio.api.controller;

import com.consultorio.application.dto.AdminPasswordResetRequest;
import com.consultorio.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación administrativa para blanqueo presencial de contraseñas.
 * Solo Recepcionista o Administrador (RN-AU-05).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")
public class AdminPasswordController {

    private final AuthService authService;

    public AdminPasswordController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> adminPasswordReset(@Valid @RequestBody AdminPasswordResetRequest request) {
        authService.adminResetPassword(request.dni(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
