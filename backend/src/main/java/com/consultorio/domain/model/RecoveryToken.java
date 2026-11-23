package com.consultorio.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token de recuperación de contraseña de un solo uso con vencimiento (RN-AU-03).
 */
@Entity
@Table(name = "recovery_tokens")
@Getter
@Setter
public class RecoveryToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;
}
