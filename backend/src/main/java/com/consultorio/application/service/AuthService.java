package com.consultorio.application.service;

import com.consultorio.application.dto.AuthResponse;
import com.consultorio.application.dto.LoginRequest;
import com.consultorio.application.dto.PatientProfileResponse;
import com.consultorio.application.dto.RegisterRequest;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RecoveryToken;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.RecoveryTokenRepository;
import com.consultorio.notification.application.port.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final RecoveryTokenRepository recoveryTokenRepository;
    private final EmailSenderPort emailSender;
    private final String baseUrl;

    @Autowired
    public AuthService(PacienteRepository pacienteRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       RecoveryTokenRepository recoveryTokenRepository,
                       EmailSenderPort emailSender,
                       @Value("${notification.base-url:http://localhost:8080}") String baseUrl) {
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.recoveryTokenRepository = recoveryTokenRepository;
        this.emailSender = emailSender;
        this.baseUrl = baseUrl;
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

    // ---------------------------------------------------------- Recuperación de contraseña (RN-AU-03)

    /**
     * Genera un token de recuperación único si el paciente tiene Gmail vinculado.
     * Responde siempre con éxito genérico para no filtrar si el DNI existe en el sistema.
     */
    @Transactional
    public void recoverPassword(String dni) {
        String dniNormalizado = dni.trim();
        Paciente paciente = pacienteRepository.findByDni(dniNormalizado)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElse(null);

        if (paciente != null && paciente.getEmailRecuperacion() != null
                && !paciente.getEmailRecuperacion().isBlank()) {
            // Invalidar tokens previos no usados para este DNI
            recoveryTokenRepository.findByDniAndUsadoFalse(dniNormalizado)
                    .ifPresent(t -> {
                        t.setUsado(true);
                        t.setFechaUso(LocalDateTime.now());
                        recoveryTokenRepository.save(t);
                    });

            RecoveryToken token = new RecoveryToken();
            token.setToken(java.util.UUID.randomUUID().toString().replace("-", ""));
            token.setDni(dniNormalizado);
            token.setFechaExpiracion(LocalDateTime.now().plusHours(1));
            token.setUsado(false);
            token.setActivo(true);
            recoveryTokenRepository.save(token);
            logger.info("Token de recuperación generado para DNI {}", dniNormalizado);

            String enlace = baseUrl + "/login.html?resetToken=" + token.getToken();
            String cuerpo = "Hola " + paciente.getNombreCompleto() + ",\n\n"
                    + "Recibimos una solicitud para restablecer tu contraseña en Open Mouth.\n"
                    + "Ingresá al siguiente enlace (válido por 1 hora):\n"
                    + enlace + "\n\n"
                    + "Si no solicitaste este cambio, podés ignorar este mensaje.";
            try {
                emailSender.enviar(
                        paciente.getEmailRecuperacion(),
                        "Open Mouth — Recuperación de contraseña",
                        cuerpo);
            } catch (RuntimeException ex) {
                logger.warn("No se pudo enviar el correo de recuperación para DNI {}: {}",
                        dniNormalizado, ex.getMessage());
            }
        }
        // Si no existe o no tiene Gmail vinculado, no hacemos nada (respuesta genérica)
    }

    /**
     * Valida el token de recuperación de un solo uso y actualiza la contraseña.
     */
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        RecoveryToken token = recoveryTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("El token de recuperación no es válido"));

        if (Boolean.TRUE.equals(token.getUsado())) {
            throw new BusinessException("El token de recuperación ya fue utilizado");
        }

        if (token.getFechaExpiracion() == null || token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El token de recuperación ha vencido");
        }

        Paciente paciente = pacienteRepository.findByDni(token.getDni())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        paciente.setPasswordHash(passwordEncoder.encode(newPassword));
        pacienteRepository.save(paciente);

        token.setUsado(true);
        token.setFechaUso(LocalDateTime.now());
        recoveryTokenRepository.save(token);

        logger.info("Contraseña restablecida para DNI {}", paciente.getDni());
    }

    // ---------------------------------------------------------- Blanqueo presencial (RN-AU-05)

    /**
     * Blanqueo presencial de contraseña por Recepcionista o Administrador.
     */
    @Transactional
    public void adminResetPassword(String dni, String newPassword) {
        String dniNormalizado = dni.trim();
        Paciente paciente = pacienteRepository.findByDni(dniNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        paciente.setPasswordHash(passwordEncoder.encode(newPassword));
        pacienteRepository.save(paciente);

        logger.info("Blanqueo presencial de contraseña para DNI {} por personal autorizado", dniNormalizado);
    }
}
