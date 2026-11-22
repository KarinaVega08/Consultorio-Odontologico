package com.consultorio.admin.domain.model;

import com.consultorio.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prestación del catálogo. Su duración define el fin de cada turno (RN-TU-03).
 */
@Entity
@Table(name = "prestaciones")
@Getter
@Setter
@NoArgsConstructor
public class Prestacion extends BaseEntity {

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "descripcion")
    private String descripcion;
}
