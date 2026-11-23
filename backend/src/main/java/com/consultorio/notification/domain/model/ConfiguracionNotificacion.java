package com.consultorio.notification.domain.model;

import com.consultorio.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Antelación configurable para los recordatorios (IN-02, default 24 h).
 */
@Entity
@Table(name = "configuracion_notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionNotificacion extends BaseEntity {

    @Column(name = "horas_antelacion", nullable = false)
    private Integer horasAntelacion;
}
