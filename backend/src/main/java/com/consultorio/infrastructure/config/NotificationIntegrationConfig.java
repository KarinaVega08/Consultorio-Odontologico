package com.consultorio.infrastructure.config;

import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Habilita el scheduler y provee un puerto no-op hasta que C-05 aporte la
 * implementación real de consulta de turnos próximos.
 */
@Configuration
@EnableScheduling
public class NotificationIntegrationConfig {

    @Bean
    @ConditionalOnMissingBean(TurnoParaRecordarPort.class)
    public TurnoParaRecordarPort noOpTurnoParaRecordarPort() {
        return (desde, hasta) -> List.of();
    }
}
