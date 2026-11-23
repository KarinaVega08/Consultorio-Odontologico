package com.consultorio.domain.model;

import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Turno de la agenda del consultorio (RN-TU-01 a RN-TU-04).
 * La baja es lógica vía {@code activo} y el estado {@code CANCELADO} libera el hueco.
 */
@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
public class Turno extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sillon_box_id", nullable = false)
    private SillonBox sillonBox;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestacion_id", nullable = false)
    private Prestacion prestacion;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTurno estado;

    @Column(name = "creado_por", nullable = false, length = 8)
    private String creadoPor;
}
