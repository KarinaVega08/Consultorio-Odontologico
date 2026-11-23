package com.consultorio.application.service;

import com.consultorio.domain.model.RolUsuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato para emisión y validación de tokens de autenticación stateless.
 */
public interface TokenService {

    String generateToken(UUID userId, String dni, RolUsuario rol);

    Optional<AuthenticatedUser> parseAndValidate(String token);

    record AuthenticatedUser(UUID userId, String dni, RolUsuario rol) {
    }
}
