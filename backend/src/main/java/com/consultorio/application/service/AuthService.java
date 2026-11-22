package com.consultorio.application.service;

import com.consultorio.application.dto.AuthResponse;
import com.consultorio.application.dto.LoginRequest;
import com.consultorio.application.dto.PatientProfileResponse;
import com.consultorio.application.dto.RegisterRequest;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.domain.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Servicio de autenticación y gestión de cuenta de pacientes (registro, login y vinculación única de Gmail).
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(PacienteRepository pacienteRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public PatientProfileResponse register(RegisterRequest request) {
        String dniNormalizado = request.dni().trim();
        if (pacienteRepository.existsByDni(dniNormalizado)) {
            throw new BusinessException("Ya existe una cuenta con este DNI");
        }

        Paciente paciente = new Paciente();
        paciente.setNombreCompleto(request.nombreCompleto().trim());
        paciente.setDni(dniNormalizado);
        paciente.setPasswordHash(passwordEncoder.encode(request.password()));

        Paciente saved = pacienteRepository.save(paciente);
        logger.info("Paciente registrado con id {}", saved.getId());
        return toProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Paciente paciente = pacienteRepository.findByDni(request.dni().trim())
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new BadCredentialsException("DNI o contraseña inválidos"));

        if (!passwordEncoder.matches(request.password(), paciente.getPasswordHash())) {
            throw new BadCredentialsException("DNI o contraseña inválidos");
        }

        String token = tokenService.generateToken(paciente.getId(), paciente.getDni(), RolUsuario.PACIENTE);
        logger.info("Login exitoso para paciente id {}", paciente.getId());
        return new AuthResponse(token, "Bearer", toProfileResponse(paciente));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile(UUID patientId) {
        Paciente paciente = findActivePatient(patientId);
        return toProfileResponse(paciente);
    }

    @Transactional
    public PatientProfileResponse linkRecoveryEmail(UUID patientId, String rawEmail) {
        Paciente paciente = findActivePatient(patientId);
        if (paciente.getEmailRecuperacion() != null && !paciente.getEmailRecuperacion().isBlank()) {
            throw new BusinessException("El correo de recuperación ya fue vinculado y no puede modificarse");
        }

        paciente.setEmailRecuperacion(rawEmail.trim().toLowerCase(Locale.ROOT));
        Paciente updated = pacienteRepository.save(paciente);
        logger.info("Correo de recuperación vinculado para paciente id {}", updated.getId());
        return toProfileResponse(updated);
    }

    private Paciente findActivePatient(UUID patientId) {
        return pacienteRepository.findById(patientId)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    private PatientProfileResponse toProfileResponse(Paciente paciente) {
        boolean requiereVincular = paciente.getEmailRecuperacion() == null
                || paciente.getEmailRecuperacion().isBlank();
        return new PatientProfileResponse(
                paciente.getId(),
                paciente.getNombreCompleto(),
                paciente.getDni(),
                paciente.getEmailRecuperacion(),
                requiereVincular,
                paciente.getFechaCreacion()
        );
    }
}
