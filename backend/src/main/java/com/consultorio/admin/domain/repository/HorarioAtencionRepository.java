package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, UUID> {

    List<HorarioAtencion> findByActivoTrue();

    List<HorarioAtencion> findByProfesionalId(UUID profesionalId);
}
