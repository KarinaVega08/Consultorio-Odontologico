package com.consultorio.notification.domain.repository;

import com.consultorio.notification.domain.model.ConfiguracionNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracionNotificacionRepository extends JpaRepository<ConfiguracionNotificacion, UUID> {

    Optional<ConfiguracionNotificacion> findFirstByActivoTrue();
}
