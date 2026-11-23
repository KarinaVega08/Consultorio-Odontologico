package com.consultorio.admin.application.service;

import com.consultorio.admin.application.dto.BloqueoRequest;
import com.consultorio.admin.application.dto.BloqueoResponse;
import com.consultorio.admin.application.dto.HorarioAtencionRequest;
import com.consultorio.admin.application.dto.HorarioAtencionResponse;
import com.consultorio.admin.application.dto.PrestacionRequest;
import com.consultorio.admin.application.dto.PrestacionResponse;
import com.consultorio.admin.application.dto.ProfesionalRequest;
import com.consultorio.admin.application.dto.ProfesionalResponse;
import com.consultorio.admin.application.dto.SillonBoxRequest;
import com.consultorio.admin.application.dto.SillonBoxResponse;
import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Cobertura CRUD de cada recurso administrativo (CHANGES.md C-03, tests por recurso).
 */
@ExtendWith(MockitoExtension.class)
class ConfiguracionConsultorioCrudTest {

    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private SillonBoxRepository sillonBoxRepository;
    @Mock private PrestacionRepository prestacionRepository;
    @Mock private HorarioAtencionRepository horarioAtencionRepository;
    @Mock private BloqueoRepository bloqueoRepository;

    @InjectMocks private ConfiguracionConsultorioService service;

    private UUID profesionalId;
    private UUID sillonId;
    private UUID recursoId;

    @BeforeEach
    void setUp() {
        profesionalId = UUID.randomUUID();
        sillonId = UUID.randomUUID();
        recursoId = UUID.randomUUID();
    }

    @Test
    void profesionalAltaEdicionListadoYBaja() {
        when(profesionalRepository.save(any(Profesional.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profesionalRepository.findById(recursoId)).thenReturn(Optional.of(profesional()));
        when(profesionalRepository.findByActivoTrue()).thenReturn(List.of(profesional()));

        ProfesionalResponse creado = service.crearProfesional(new ProfesionalRequest("Dra. Ana Ficticia", "MP-99999"));
        ProfesionalResponse editado = service.actualizarProfesional(recursoId, new ProfesionalRequest("Dra. Ana Actualizada", "MP-99998"));
        List<ProfesionalResponse> listado = service.listarProfesionales();
        ProfesionalResponse dadoDeBaja = service.activarProfesional(recursoId, false);

        assertThat(creado.nombreCompleto()).isEqualTo("Dra. Ana Ficticia");
        assertThat(editado.nombreCompleto()).isEqualTo("Dra. Ana Actualizada");
        assertThat(editado.matricula()).isEqualTo("MP-99998");
        assertThat(listado).hasSize(1);
        assertThat(dadoDeBaja.activo()).isFalse();
    }

    @Test
    void sillonBoxAltaEdicionListadoYBaja() {
        when(sillonBoxRepository.save(any(SillonBox.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sillonBoxRepository.findById(recursoId)).thenReturn(Optional.of(sillonBox()));
        when(sillonBoxRepository.findByActivoTrue()).thenReturn(List.of(sillonBox()));

        SillonBoxResponse creado = service.crearSillonBox(new SillonBoxRequest("Sillón 1"));
        SillonBoxResponse editado = service.actualizarSillonBox(recursoId, new SillonBoxRequest("Sillón 2"));
        List<SillonBoxResponse> listado = service.listarSillonesBoxes();
        SillonBoxResponse dadoDeBaja = service.activarSillonBox(recursoId, false);

        assertThat(creado.nombre()).isEqualTo("Sillón 1");
        assertThat(editado.nombre()).isEqualTo("Sillón 2");
        assertThat(listado).hasSize(1);
        assertThat(dadoDeBaja.activo()).isFalse();
    }

    @Test
    void prestacionAltaEdicionListadoYBaja() {
        when(prestacionRepository.save(any(Prestacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(prestacionRepository.findById(recursoId)).thenReturn(Optional.of(prestacion()));
        when(prestacionRepository.findByActivoTrue()).thenReturn(List.of(prestacion()));

        PrestacionResponse creado = service.crearPrestacion(new PrestacionRequest("Consulta", 30, "Ficticio"));
        PrestacionResponse editado = service.actualizarPrestacion(recursoId, new PrestacionRequest("Endodoncia", 60, "Ficticio"));
        List<PrestacionResponse> listado = service.listarPrestaciones();
        PrestacionResponse dadoDeBaja = service.activarPrestacion(recursoId, false);

        assertThat(creado.duracionMinutos()).isEqualTo(30);
        assertThat(editado.duracionMinutos()).isEqualTo(60);
        assertThat(listado).hasSize(1);
        assertThat(dadoDeBaja.activo()).isFalse();
    }

    @Test
    void horarioAltaEdicionListadoYBaja() {
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesional()));
        when(horarioAtencionRepository.save(any(HorarioAtencion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(horarioAtencionRepository.findById(recursoId)).thenReturn(Optional.of(horario()));
        when(horarioAtencionRepository.findByActivoTrue()).thenReturn(List.of(horario()));
        when(horarioAtencionRepository.findByProfesionalId(profesionalId)).thenReturn(List.of(horario()));

        var creado = service.crearHorario(horarioRequest(1, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        var editado = service.actualizarHorario(recursoId, horarioRequest(2, LocalTime.of(10, 0), LocalTime.of(18, 0)));
        List<HorarioAtencionResponse> listado = service.listarHorarios();
        List<HorarioAtencionResponse> porProfesional = service.listarHorariosPorProfesional(profesionalId);
        var dadoDeBaja = service.activarHorario(recursoId, false);

        assertThat(creado.diaSemana()).isEqualTo(1);
        assertThat(editado.diaSemana()).isEqualTo(2);
        assertThat(editado.horaInicio()).isEqualTo(LocalTime.of(10, 0));
        assertThat(listado).hasSize(1);
        assertThat(porProfesional).hasSize(1);
        assertThat(dadoDeBaja.activo()).isFalse();
    }

    @Test
    void bloqueoAltaEdicionListadoYBaja() {
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findById(sillonId)).thenReturn(Optional.of(sillonBox()));
        when(bloqueoRepository.save(any(Bloqueo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bloqueoRepository.findById(recursoId)).thenReturn(Optional.of(bloqueo()));
        when(bloqueoRepository.findByActivoTrue()).thenReturn(List.of(bloqueo()));
        when(bloqueoRepository.findByProfesionalId(profesionalId)).thenReturn(List.of(bloqueo()));
        when(bloqueoRepository.findBySillonBoxId(sillonId)).thenReturn(List.of(bloqueo()));

        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 10, 0);
        BloqueoResponse creado = service.crearBloqueo(bloqueoRequest(profesionalId, sillonId, inicio, "Capacitación"));
        BloqueoResponse editado = service.actualizarBloqueo(recursoId, bloqueoRequest(profesionalId, sillonId, inicio.plusHours(1), "Mantenimiento"));
        List<BloqueoResponse> listado = service.listarBloqueos();
        List<BloqueoResponse> porProfesional = service.listarBloqueosPorProfesional(profesionalId);
        List<BloqueoResponse> porSillon = service.listarBloqueosPorSillonBox(sillonId);
        BloqueoResponse dadoDeBaja = service.activarBloqueo(recursoId, false);

        assertThat(creado.motivo()).isEqualTo("Capacitación");
        assertThat(editado.motivo()).isEqualTo("Mantenimiento");
        assertThat(editado.fechaHoraInicio()).isEqualTo(inicio.plusHours(1));
        assertThat(listado).hasSize(1);
        assertThat(porProfesional).hasSize(1);
        assertThat(porSillon).hasSize(1);
        assertThat(dadoDeBaja.activo()).isFalse();
    }

    @Test
    void actualizarRecursosInexistentesDevuelve404() {
        when(profesionalRepository.findById(recursoId)).thenReturn(Optional.empty());
        when(sillonBoxRepository.findById(recursoId)).thenReturn(Optional.empty());
        when(prestacionRepository.findById(recursoId)).thenReturn(Optional.empty());
        when(horarioAtencionRepository.findById(recursoId)).thenReturn(Optional.empty());
        when(bloqueoRepository.findById(recursoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarProfesional(recursoId, new ProfesionalRequest("Dra. Ana Ficticia", null)))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.actualizarSillonBox(recursoId, new SillonBoxRequest("Sillón 1")))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.actualizarPrestacion(recursoId, new PrestacionRequest("Consulta", 30, null)))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.actualizarHorario(recursoId, horarioRequest(1, LocalTime.of(9, 0), LocalTime.of(17, 0))))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.actualizarBloqueo(
            recursoId, bloqueoRequest(profesionalId, sillonId, LocalDateTime.of(2026, 11, 25, 10, 0), "Ficticio")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private HorarioAtencionRequest horarioRequest(int dia, LocalTime inicio, LocalTime fin) {
        return new HorarioAtencionRequest(profesionalId, dia, inicio, fin);
    }

    private BloqueoRequest bloqueoRequest(UUID profesional, UUID sillon, LocalDateTime inicio, String motivo) {
        return new BloqueoRequest(profesional, sillon, inicio, inicio.plusHours(1), motivo);
    }

    private Profesional profesional() {
        Profesional profesional = new Profesional();
        profesional.setId(profesionalId);
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        profesional.setMatricula("MP-99999");
        return profesional;
    }

    private SillonBox sillonBox() {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setId(sillonId);
        sillonBox.setNombre("Sillón 1");
        return sillonBox;
    }

    private Prestacion prestacion() {
        Prestacion prestacion = new Prestacion();
        prestacion.setId(recursoId);
        prestacion.setNombre("Consulta");
        prestacion.setDuracionMinutos(30);
        return prestacion;
    }

    private HorarioAtencion horario() {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setId(recursoId);
        horario.setProfesional(profesional());
        horario.setDiaSemana(1);
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(17, 0));
        return horario;
    }

    private Bloqueo bloqueo() {
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setId(recursoId);
        bloqueo.setProfesional(profesional());
        bloqueo.setSillonBox(sillonBox());
        bloqueo.setFechaHoraInicio(LocalDateTime.of(2026, 11, 25, 10, 0));
        bloqueo.setFechaHoraFin(LocalDateTime.of(2026, 11, 25, 11, 0));
        return bloqueo;
    }
}
