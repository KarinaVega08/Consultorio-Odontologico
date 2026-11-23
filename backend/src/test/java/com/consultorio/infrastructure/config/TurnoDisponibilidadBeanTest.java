package com.consultorio.infrastructure.config;

import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import com.consultorio.infrastructure.persistence.TurnoDisponibilidadAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que el bean real de C-05 reemplaza al no-op de C-04 sin editar su configuración.
 */
@SpringBootTest
class TurnoDisponibilidadBeanTest {

    @Autowired private TurnoDisponibilidadPort port;

    @Test
    void elContextoUsaElAdaptadorRealEnLugarDelFallback() {
        assertThat(port).isInstanceOf(TurnoDisponibilidadAdapter.class);
        assertThat(port).isNotInstanceOf(NoOpTurnoDisponibilidadPort.class);
    }
}
