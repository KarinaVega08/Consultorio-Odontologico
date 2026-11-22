package com.consultorio.admin.infrastructure.persistence;

import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistencia y baja lógica de los recursos administrativos con H2 (sin servicios externos).
 */
@DataJpaTest
class AdminRepositoriesTest {

    @Autowired private ProfesionalRepository profesionalRepository;
    @Autowired private SillonBoxRepository sillonBoxRepository;
    @Autowired private HorarioAtencionRepository horarioAtencionRepository;
    @Autowired private BloqueoRepository bloqueoRepository;

    @Test
    void profesionalSeListaActivoYDesapareceConBajaLogica() {
        Profesional profesional = profesionalFicticio();
        profesionalRepository.saveAndFlush(profesional);

        assertThat(profesionalRepository.findByActivoTrue()).extracting(Profesional::getId)
            .contains(profesional.getId());

        profesional.setActivo(false);
        profesionalRepository.saveAndFlush(profesional);

        assertThat(profesionalRepository.findByActivoTrue()).isEmpty();
    }

    @Test
    void sillonBoxSeListaActivoYDesapareceConBajaLogica() {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setNombre("Sillón 1");
        sillonBoxRepository.saveAndFlush(sillonBox);

        assertThat(sillonBoxRepository.findByActivoTrue()).hasSize(1);

        sillonBox.setActivo(false);
        sillonBoxRepository.saveAndFlush(sillonBox);

        assertThat(sillonBoxRepository.findByActivoTrue()).isEmpty();
    }

    @Test
    void horariosSeFiltranPorProfesionalYRespetaBajaLogica() {
        Profesional profesional = profesionalRepository.saveAndFlush(profesionalFicticio());
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(1);
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(17, 0));
        horarioAtencionRepository.saveAndFlush(horario);

        assertThat(horarioAtencionRepository.findByProfesionalId(profesional.getId())).hasSize(1);
        assertThat(horarioAtencionRepository.findByActivoTrue()).hasSize(1);

        horario.setActivo(false);
        horarioAtencionRepository.saveAndFlush(horario);

        assertThat(horarioAtencionRepository.findByActivoTrue()).isEmpty();
    }

    @Test
    void bloqueosSeFiltranPorProfesionalYSillonYRespetaBajaLogica() {
        Profesional profesional = profesionalRepository.saveAndFlush(profesionalFicticio());
        SillonBox sillonBox = sillonBoxRepository.saveAndFlush(sillonBoxFicticio());
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setProfesional(profesional);
        bloqueo.setSillonBox(sillonBox);
        bloqueo.setFechaHoraInicio(LocalDateTime.of(2026, 11, 25, 10, 0));
        bloqueo.setFechaHoraFin(LocalDateTime.of(2026, 11, 25, 11, 0));
        bloqueo.setMotivo("Capacitación ficticia");
        bloqueoRepository.saveAndFlush(bloqueo);

        assertThat(bloqueoRepository.findByProfesionalId(profesional.getId())).hasSize(1);
        assertThat(bloqueoRepository.findBySillonBoxId(sillonBox.getId())).hasSize(1);
        assertThat(bloqueoRepository.findByActivoTrue()).hasSize(1);

        bloqueo.setActivo(false);
        bloqueoRepository.saveAndFlush(bloqueo);

        assertThat(bloqueoRepository.findByActivoTrue()).isEmpty();
    }

    private Profesional profesionalFicticio() {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        profesional.setMatricula("MP-99999");
        return profesional;
    }

    private SillonBox sillonBoxFicticio() {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setNombre("Sillón 1");
        return sillonBox;
    }
}
