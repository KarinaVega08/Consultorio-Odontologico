package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.Prestacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrestacionRepository extends JpaRepository<Prestacion, UUID> {

    List<Prestacion> findByActivoTrue();
}
