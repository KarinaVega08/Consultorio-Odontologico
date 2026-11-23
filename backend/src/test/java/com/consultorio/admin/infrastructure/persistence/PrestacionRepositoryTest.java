package com.consultorio.admin.infrastructure.persistence;

import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PrestacionRepositoryTest {

    @Autowired
    private PrestacionRepository prestacionRepository;

    @Test
    void listarActivasIncluyePrestacionFicticia() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Consulta/Diagnóstico");
        prestacion.setDuracionMinutos(30);
        prestacion.setDescripcion("Dato ficticio de prueba");
        prestacion.setActivo(true);
        prestacionRepository.saveAndFlush(prestacion);

        assertThat(prestacionRepository.findByActivoTrue())
            .extracting(Prestacion::getNombre)
            .contains("Consulta/Diagnóstico");
    }

    @Test
    void bajaLogicaExcluyePrestacionDelListadoActivo() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Limpieza");
        prestacion.setDuracionMinutos(30);
        prestacion.setActivo(true);
        prestacionRepository.saveAndFlush(prestacion);

        prestacion.setActivo(false);
        prestacionRepository.saveAndFlush(prestacion);

        assertThat(prestacionRepository.findByActivoTrue()).isEmpty();
    }
}
