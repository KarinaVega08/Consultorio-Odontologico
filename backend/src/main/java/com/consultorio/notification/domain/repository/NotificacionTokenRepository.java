package com.consultorio.notification.domain.repository;

import com.consultorio.notification.domain.model.NotificacionToken;
import com.consultorio.notification.domain.model.TipoNotificacionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificacionTokenRepository extends JpaRepository<NotificacionToken, UUID> {

    Optional<NotificacionToken> findByToken(String token);

    boolean existsByTurnoIdAndTipoAndUsadoFalse(UUID turnoId, TipoNotificacionToken tipo);
}
