package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfesionalRepository extends JpaRepository<Profesional, UUID> {

    List<Profesional> findByActivoTrue();
}
