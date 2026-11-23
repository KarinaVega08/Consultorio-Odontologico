package com.consultorio.api.controller;

import com.consultorio.application.dto.AuthResponse;
import com.consultorio.application.dto.LoginRequest;
import com.consultorio.application.dto.PatientProfileResponse;
import com.consultorio.application.dto.RecoveryRequest;
import com.consultorio.application.dto.RegisterRequest;
import com.consultorio.application.dto.ResetPasswordRequest;
import com.consultorio.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import com.consultorio.application.service.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST público para registro, login y recuperación de contraseña de pacientes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<PatientProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        PatientProfileResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<Void> recoverPassword(@Valid @RequestBody RecoveryRequest request) {
        authService.recoverPassword(request.dni());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
