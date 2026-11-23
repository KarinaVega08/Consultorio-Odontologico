package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.SillonBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SillonBoxRepository extends JpaRepository<SillonBox, UUID> {

    List<SillonBox> findByActivoTrue();

    /** Serializa las reservas simultáneas sobre el mismo sillón/box (D2, IN-04). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SillonBox s where s.id = :id")
    Optional<SillonBox> findByIdForUpdate(@Param("id") UUID id);
}
