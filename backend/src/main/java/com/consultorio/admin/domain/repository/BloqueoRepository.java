package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BloqueoRepository extends JpaRepository<Bloqueo, UUID> {

    List<Bloqueo> findByActivoTrue();

    List<Bloqueo> findByProfesionalId(UUID profesionalId);

    List<Bloqueo> findBySillonBoxId(UUID sillonBoxId);
}
