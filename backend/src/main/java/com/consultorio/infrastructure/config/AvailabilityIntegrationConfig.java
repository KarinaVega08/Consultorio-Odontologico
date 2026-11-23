package com.consultorio.infrastructure.config;

import com.consultorio.domain.availability.AvailabilityCalculator;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hasta que C-05 se integre, expone un puerto vacío para que el contexto arranque.
 * Cuando exista un bean real, este fallback se retrasa y no se registra.
 */
@Configuration
@EnableConfigurationProperties(AvailabilityProperties.class)
public class AvailabilityIntegrationConfig {

    @Bean
    @ConditionalOnMissingBean(TurnoDisponibilidadPort.class)
    public TurnoDisponibilidadPort noOpTurnoDisponibilidadPort() {
        return new NoOpTurnoDisponibilidadPort();
    }

    @Bean
    public AvailabilityCalculator availabilityCalculator() {
        return new AvailabilityCalculator();
    }
}
