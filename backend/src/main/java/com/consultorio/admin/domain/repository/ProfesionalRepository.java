package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfesionalRepository extends JpaRepository<Profesional, UUID> {

    List<Profesional> findByActivoTrue();

    /** Serializa las reservas simultáneas sobre el mismo profesional (D2, IN-04). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profesional p where p.id = :id")
    Optional<Profesional> findByIdForUpdate(@Param("id") UUID id);
}
