package com.consultorio.application.service;

import com.consultorio.application.dto.AuthResponse;
import com.consultorio.application.dto.LoginRequest;
import com.consultorio.application.dto.PatientProfileResponse;
import com.consultorio.application.dto.RegisterRequest;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.domain.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de AuthService (datos ficticios según Ley 25.326).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private TokenService tokenService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(pacienteRepository, passwordEncoder, tokenService);
    }

    @Test
    void registerShouldHashPasswordAndRequireGmailLink() {
        when(pacienteRepository.existsByDni("99999991")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setFechaCreacion(Instant.now());
            return p;
        });

        PatientProfileResponse response = authService.register(
                new RegisterRequest("Juan Pérez", "99999991", "ClaveSegura123"));

        assertEquals("99999991", response.dni());
        assertTrue(response.requiereVincularGmail());
    }

    @Test
    void registerShouldRejectDuplicateDni() {
        when(pacienteRepository.existsByDni("99999992")).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                authService.register(new RegisterRequest("Ana Test", "99999992", "ClaveSegura123")));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(id);
        paciente.setNombreCompleto("Juan Pérez");
        paciente.setDni("99999993");
        paciente.setPasswordHash(passwordEncoder.encode("ClaveValida1"));
        paciente.setActivo(true);

        when(pacienteRepository.findByDni("99999993")).thenReturn(Optional.of(paciente));
        when(tokenService.generateToken(id, "99999993", RolUsuario.PACIENTE)).thenReturn("token-ficticio");

        AuthResponse response = authService.login(new LoginRequest("99999993", "ClaveValida1"));

        assertEquals("token-ficticio", response.token());
        assertEquals("Bearer", response.tokenType());
        assertTrue(response.patient().requiereVincularGmail());
    }

    @Test
    void loginShouldThrowWhenPasswordIsIncorrect() {
        Paciente paciente = new Paciente();
        paciente.setId(UUID.randomUUID());
        paciente.setDni("99999993");
        paciente.setPasswordHash(passwordEncoder.encode("ClaveValida1"));
        paciente.setActivo(true);

        when(pacienteRepository.findByDni("99999993")).thenReturn(Optional.of(paciente));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("99999993", "Incorrecta")));
    }

    @Test
    void linkRecoveryEmailShouldSucceedOnceAndRejectSecondAttempt() {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(id);
        paciente.setNombreCompleto("Juan Pérez");
        paciente.setDni("99999994");
        paciente.setPasswordHash("hash");
        paciente.setActivo(true);

        when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientProfileResponse linked = authService.linkRecoveryEmail(id, "Juan.Perez.Test@gmail.com");
        assertEquals("juan.perez.test@gmail.com", linked.emailRecuperacion());
        assertFalse(linked.requiereVincularGmail());
        assertNotEquals("hash", linked.dni());

        assertThrows(BusinessException.class, () ->
                authService.linkRecoveryEmail(id, "otro.test@gmail.com"));
    }
}
