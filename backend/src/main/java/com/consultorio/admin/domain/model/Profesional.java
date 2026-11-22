package com.consultorio.admin.domain.model;

import com.consultorio.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profesional del consultorio (RN-TU-05). La baja es lógica vía {@code activo}.
 */
@Entity
@Table(name = "profesionales")
@Getter
@Setter
@NoArgsConstructor
public class Profesional extends BaseEntity {

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "matricula")
    private String matricula;
}
