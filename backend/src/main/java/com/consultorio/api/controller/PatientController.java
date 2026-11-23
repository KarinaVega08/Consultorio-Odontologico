package com.consultorio.api.controller;

import com.consultorio.application.dto.LinkRecoveryEmailRequest;
import com.consultorio.application.dto.PatientProfileResponse;
import com.consultorio.application.service.AuthService;
import com.consultorio.application.service.TokenService.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST autenticado para el perfil y vinculación única de Gmail del paciente.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final AuthService authService;

    public PatientController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<PatientProfileResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(authService.getProfile(user.userId()));
    }

    @PostMapping("/me/recovery-email")
    public ResponseEntity<PatientProfileResponse> linkRecoveryEmail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody LinkRecoveryEmailRequest request) {
        return ResponseEntity.ok(authService.linkRecoveryEmail(user.userId(), request.email()));
    }
}
