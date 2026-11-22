package com.consultorio.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de dominio que representa a un paciente del consultorio odontológico.
 * El DNI (7-8 dígitos) actúa como identificador único de acceso.
 */
@Entity
@Table(name = "pacientes")
@Getter
@Setter
public class Paciente extends BaseEntity {

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "email_recuperacion", length = 180)
    private String emailRecuperacion;
}
