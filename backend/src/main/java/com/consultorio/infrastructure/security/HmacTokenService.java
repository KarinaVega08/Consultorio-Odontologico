package com.consultorio.infrastructure.security;

import com.consultorio.application.service.TokenService;
import com.consultorio.domain.model.RolUsuario;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación stateless de tokens firmados con HMAC-SHA256.
 * Formato compacto: base64url(userId|dni|rol|expEpochSec).base64url(hmac)
 */
public class HmacTokenService implements TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] secretBytes;
    private final long expirationSeconds;

    public HmacTokenService(String secret, long expirationSeconds) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(UUID userId, String dni, RolUsuario rol) {
        long exp = Instant.now().getEpochSecond() + expirationSeconds;
        String payload = userId + "|" + dni + "|" + rol.name() + "|" + exp;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    @Override
    public Optional<AuthenticatedUser> parseAndValidate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }
        String encodedPayload = parts[0];
        String expectedSignature = sign(encodedPayload);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            String[] fields = payload.split("\\|");
            if (fields.length != 4) {
                return Optional.empty();
            }
            long exp = Long.parseLong(fields[3]);
            if (Instant.now().getEpochSecond() > exp) {
                return Optional.empty();
            }
            UUID userId = UUID.fromString(fields[0]);
            String dni = fields[1];
            RolUsuario rol = RolUsuario.valueOf(fields[2]);
            return Optional.of(new AuthenticatedUser(userId, dni, rol));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token de autenticación", ex);
        }
    }
}
