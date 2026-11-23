package com.consultorio.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la grilla de disponibilidad (IN-05).
 */
@ConfigurationProperties(prefix = "availability")
public record AvailabilityProperties(int gridMinutes) {

    public AvailabilityProperties {
        if (gridMinutes <= 0) {
            gridMinutes = 15;
        }
    }
}
