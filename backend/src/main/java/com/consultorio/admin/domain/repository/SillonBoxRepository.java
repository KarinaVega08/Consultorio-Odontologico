package com.consultorio.admin.domain.repository;

import com.consultorio.admin.domain.model.SillonBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SillonBoxRepository extends JpaRepository<SillonBox, UUID> {

    List<SillonBox> findByActivoTrue();
}
