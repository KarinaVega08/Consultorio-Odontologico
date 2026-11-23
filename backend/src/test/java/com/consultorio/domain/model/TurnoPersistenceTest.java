package com.consultorio.domain.model;

import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistencia de la entidad Turno con auditoría de BaseEntity (datos ficticios, Ley 25.326).
 */
@DataJpaTest
class TurnoPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persisteTurnoYRecuperaConFechaCreacion() {
        Paciente paciente = new Paciente();
        paciente.setNombreCompleto("Paciente Ficticio");
        paciente.setDni("99999999");
        paciente.setPasswordHash("hash-ficticio");
        entityManager.persistAndFlush(paciente);

        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        profesional.setMatricula("MP-99999");
        entityManager.persistAndFlush(profesional);

        SillonBox sillonBox = new SillonBox();
        sillonBox.setNombre("Sillón 1");
        entityManager.persistAndFlush(sillonBox);

        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Consulta");
        prestacion.setDuracionMinutos(30);
        entityManager.persistAndFlush(prestacion);

        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 9, 0);
        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setSillonBox(sillonBox);
        turno.setPrestacion(prestacion);
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(inicio.plusMinutes(30));
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCreadoPor("99999999");
        entityManager.persistAndFlush(turno);

        entityManager.clear();

        Turno recuperado = entityManager.find(Turno.class, turno.getId());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getFechaCreacion()).isNotNull();
        assertThat(recuperado.getFechaActualizacion()).isNotNull();
        assertThat(recuperado.getEstado()).isEqualTo(EstadoTurno.RESERVADO);
        assertThat(recuperado.getFechaHoraFin()).isEqualTo(inicio.plusMinutes(30));
        assertThat(recuperado.getCreadoPor()).isEqualTo("99999999");
        assertThat(recuperado.getPaciente().getDni()).isEqualTo("99999999");
    }
}
