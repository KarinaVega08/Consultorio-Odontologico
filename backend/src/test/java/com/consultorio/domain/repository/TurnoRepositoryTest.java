package com.consultorio.domain.repository;

import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consultas de solapamiento y bloqueo pesimista sobre los recursos contendientes.
 */
@DataJpaTest
class TurnoRepositoryTest {

    @Autowired private TurnoRepository turnoRepository;
    @Autowired private ProfesionalRepository profesionalRepository;
    @Autowired private SillonBoxRepository sillonBoxRepository;
    @Autowired private PrestacionRepository prestacionRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private Profesional profesional;
    private Profesional otroProfesional;
    private SillonBox sillonBox;
    private Prestacion prestacion;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        profesional = profesionalRepository.saveAndFlush(profesional("Dra. Ana Ficticia"));
        otroProfesional = profesionalRepository.saveAndFlush(profesional("Dr. Luis Ficticio"));
        sillonBox = sillonBoxRepository.saveAndFlush(sillon("Sillón 1"));
        prestacion = prestacionRepository.saveAndFlush(prestacion());
        paciente = pacienteRepository.saveAndFlush(pacienteFicticio());
    }

    @Test
    void findSolapesPorProfesionalExcluyeCanceladosBordeYSelf() {
        Turno ocupado = turnoRepository.saveAndFlush(turno(profesional, sillonBox, 9, 10, EstadoTurno.RESERVADO));
        turnoRepository.saveAndFlush(turno(profesional, sillonBox, 11, 12, EstadoTurno.CANCELADO));

        List<Turno> solapes = turnoRepository.findSolapesPorProfesional(
            profesional.getId(), hora(9, 30), hora(10, 30), null);
        assertThat(solapes).extracting(Turno::getId).containsExactly(ocupado.getId());

        assertThat(turnoRepository.findSolapesPorProfesional(
            profesional.getId(), hora(10), hora(11), null)).isEmpty();

        assertThat(turnoRepository.findSolapesPorProfesional(
            profesional.getId(), hora(9, 30), hora(10, 30), ocupado.getId())).isEmpty();
    }

    @Test
    void findSolapesPorSillonDetectaOtroProfesionalYExcluyeCancelados() {
        turnoRepository.saveAndFlush(turno(otroProfesional, sillonBox, 10, 11, EstadoTurno.CONFIRMADO));
        turnoRepository.saveAndFlush(turno(profesional, sillonBox, 12, 13, EstadoTurno.CANCELADO));

        List<Turno> solapes = turnoRepository.findSolapesPorSillon(
            sillonBox.getId(), hora(10, 30), hora(11), null);
        assertThat(solapes).hasSize(1);
        assertThat(solapes.get(0).getProfesional().getId()).isEqualTo(otroProfesional.getId());

        assertThat(turnoRepository.findSolapesPorSillon(
            sillonBox.getId(), hora(11), hora(12), null)).isEmpty();
    }

    @Test
    void locksPesimistasSobreProfesionalYSillonEstanDisponibles() {
        assertThat(profesionalRepository.findByIdForUpdate(profesional.getId())).isPresent();
        assertThat(sillonBoxRepository.findByIdForUpdate(sillonBox.getId())).isPresent();
    }

    private static LocalDateTime hora(int hora) {
        return hora(hora, 0);
    }

    private static LocalDateTime hora(int hora, int minuto) {
        return LocalDateTime.of(2026, 11, 25, hora, minuto);
    }

    private Turno turno(Profesional profesional, SillonBox sillonBox, int inicio, int fin, EstadoTurno estado) {
        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setSillonBox(sillonBox);
        turno.setPrestacion(prestacion);
        turno.setFechaHoraInicio(hora(inicio, 0));
        turno.setFechaHoraFin(hora(fin, 0));
        turno.setEstado(estado);
        turno.setCreadoPor(paciente.getDni());
        return turno;
    }

    private static Profesional profesional(String nombre) {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto(nombre);
        profesional.setMatricula("MP-99999");
        return profesional;
    }

    private static SillonBox sillon(String nombre) {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setNombre(nombre);
        return sillonBox;
    }

    private static Prestacion prestacion() {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Consulta");
        prestacion.setDuracionMinutos(30);
        return prestacion;
    }

    private static Paciente pacienteFicticio() {
        Paciente paciente = new Paciente();
        paciente.setNombreCompleto("Paciente Ficticio");
        paciente.setDni("99999998");
        paciente.setPasswordHash("hash-ficticio");
        return paciente;
    }
}
