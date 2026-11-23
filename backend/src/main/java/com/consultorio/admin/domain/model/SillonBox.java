package com.consultorio.admin.domain.model;

import com.consultorio.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sillón o box físico del consultorio (RN-TU-02).
 */
@Entity
@Table(name = "sillones_boxes")
@Getter
@Setter
@NoArgsConstructor
public class SillonBox extends BaseEntity {

    @Column(name = "nombre", nullable = false)
    private String nombre;
}
