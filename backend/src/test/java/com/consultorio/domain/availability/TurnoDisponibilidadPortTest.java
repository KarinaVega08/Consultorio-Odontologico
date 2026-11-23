package com.consultorio.domain.availability;

import com.consultorio.infrastructure.config.NoOpTurnoDisponibilidadPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TurnoDisponibilidadPortTest {

    private final TurnoDisponibilidadPort port = new NoOpTurnoDisponibilidadPort();
    private final UUID profesionalId = UUID.randomUUID();
    private final LocalDate fecha = LocalDate.of(2026, 11, 25);

    @Test
    void sinC05NoHayTurnosOcupados() {
        assertThat(port.findTurnosOcupados(profesionalId, fecha, null)).isEmpty();
    }

    @Test
    void fallbackConSillonNoFalla() {
        UUID sillonBoxId = UUID.randomUUID();

        assertThatCode(() -> port.findTurnosOcupados(profesionalId, fecha, sillonBoxId))
            .doesNotThrowAnyException();
        assertThat(port.findTurnosOcupados(profesionalId, fecha, sillonBoxId)).isEmpty();
    }
}
