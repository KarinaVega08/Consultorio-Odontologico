package com.consultorio.notification.domain.model;

import com.consultorio.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enlace único de confirmación/cancelación o marco de recordatorio ya enviado.
 * Un solo uso y con vencimiento (RN-NT-02).
 */
@Entity
@Table(name = "notificacion_tokens")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoNotificacionToken tipo;

    @Column(name = "turno_id", nullable = false)
    private UUID turnoId;

    @Column(name = "email_destino", nullable = false)
    private String emailDestino;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;
}
