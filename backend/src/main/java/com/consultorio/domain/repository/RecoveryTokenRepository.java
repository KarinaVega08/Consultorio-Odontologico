package com.consultorio.domain.repository;

import com.consultorio.domain.model.RecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, UUID> {

    Optional<RecoveryToken> findByToken(String token);

    boolean existsByDniAndUsadoFalse(String dni);

    Optional<RecoveryToken> findByDniAndUsadoFalse(String dni);
}
