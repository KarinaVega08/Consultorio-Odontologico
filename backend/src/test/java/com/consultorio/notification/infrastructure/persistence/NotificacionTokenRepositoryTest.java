package com.consultorio.notification.infrastructure.persistence;

import com.consultorio.notification.domain.model.NotificacionToken;
import com.consultorio.notification.domain.model.TipoNotificacionToken;
import com.consultorio.notification.domain.repository.NotificacionTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificacionTokenRepositoryTest {

    @Autowired
    private NotificacionTokenRepository repository;

    @Test
    void buscarTokenUnicoYVerificarSiYaHayRecordatorioActivo() {
        UUID turnoId = UUID.randomUUID();
        NotificacionToken token = new NotificacionToken();
        token.setToken("tokenunico123");
        token.setTipo(TipoNotificacionToken.RECORDATORIO);
        token.setTurnoId(turnoId);
        token.setEmailDestino("paciente.ficticio@example.com");
        token.setFechaExpiracion(LocalDateTime.now().plusDays(2));
        token.setUsado(false);
        repository.saveAndFlush(token);

        assertThat(repository.findByToken("tokenunico123")).isPresent();
        assertThat(repository.existsByTurnoIdAndTipoAndUsadoFalse(
            turnoId, TipoNotificacionToken.RECORDATORIO)).isTrue();
        assertThat(repository.existsByTurnoIdAndTipoAndUsadoFalse(
            UUID.randomUUID(), TipoNotificacionToken.RECORDATORIO)).isFalse();
    }
}
