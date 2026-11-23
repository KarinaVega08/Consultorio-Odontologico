package com.consultorio.application.service;

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
import com.consultorio.application.dto.AvailabilityRequest;
import com.consultorio.application.dto.AvailabilityResponse;
import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.availability.AvailabilityCalculator;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import com.consultorio.domain.exception.ResourceNotFoundException;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.infrastructure.config.AvailabilityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 25);

    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private PrestacionRepository prestacionRepository;
    @Mock private SillonBoxRepository sillonBoxRepository;
    @Mock private HorarioAtencionRepository horarioAtencionRepository;
    @Mock private BloqueoRepository bloqueoRepository;
    @Mock private TurnoDisponibilidadPort turnoDisponibilidadPort;

    private AvailabilityService service;
    private UUID profesionalId;
    private UUID prestacionId;

    @BeforeEach
    void setUp() {
        service = new AvailabilityService(
            profesionalRepository, prestacionRepository, sillonBoxRepository,
            horarioAtencionRepository, bloqueoRepository, turnoDisponibilidadPort,
            new AvailabilityCalculator(), new AvailabilityProperties(15));
        profesionalId = UUID.randomUUID();
        prestacionId = UUID.randomUUID();
    }

    @Test
    void consultaConHorarioSinBloqueosDevuelveFranjas() {
        stubProfesionalYPrestacion(true);
        when(horarioAtencionRepository.findByProfesionalId(profesionalId))
            .thenReturn(List.of(horario(profesionalId, LocalTime.of(9, 0), LocalTime.of(12, 0))));
        when(bloqueoRepository.findByProfesionalId(profesionalId)).thenReturn(List.of());
        when(turnoDisponibilidadPort.findTurnosOcupados(any(), any(), any())).thenReturn(List.of());

        AvailabilityResponse response = service.consultar(request(null));

        assertThat(response.profesionalId()).isEqualTo(profesionalId);
        assertThat(response.duracionMinutos()).isEqualTo(30);
        assertThat(response.franjas()).extracting(franja -> franja.inicio().toLocalTime())
            .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
                LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void sinHorarioDevuelveFranjasVacias() {
        stubProfesionalYPrestacion(true);
        when(horarioAtencionRepository.findByProfesionalId(profesionalId)).thenReturn(List.of());
        when(bloqueoRepository.findByProfesionalId(profesionalId)).thenReturn(List.of());
        when(turnoDisponibilidadPort.findTurnosOcupados(any(), any(), any())).thenReturn(List.of());

        AvailabilityResponse response = service.consultar(request(null));

        assertThat(response.franjas()).isEmpty();
    }

    @Test
    void profesionalInexistenteLanza404() {
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultar(request(null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void profesionalInactivoLanza404() {
        when(profesionalRepository.findById(profesionalId))
            .thenReturn(Optional.of(profesional(profesionalId, false)));

        assertThatThrownBy(() -> service.consultar(request(null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void prestacionInexistenteLanza404() {
        when(profesionalRepository.findById(profesionalId))
            .thenReturn(Optional.of(profesional(profesionalId, true)));
        when(prestacionRepository.findById(prestacionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultar(request(null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void bloqueosDeProfesionalYSillonSeAplicanJuntos() {
        UUID sillonId = UUID.randomUUID();
        stubProfesionalYPrestacion(true);
        when(sillonBoxRepository.findById(sillonId))
            .thenReturn(Optional.of(sillonBox(sillonId, true)));
        when(horarioAtencionRepository.findByProfesionalId(profesionalId))
            .thenReturn(List.of(horario(profesionalId, LocalTime.of(9, 0), LocalTime.of(12, 0))));
        when(bloqueoRepository.findByProfesionalId(profesionalId))
            .thenReturn(List.of(bloqueo(profesionalId, null, LocalTime.of(9, 0), LocalTime.of(9, 30))));
        when(bloqueoRepository.findBySillonBoxId(sillonId))
            .thenReturn(List.of(bloqueo(null, sillonId, LocalTime.of(10, 0), LocalTime.of(11, 0))));
        when(turnoDisponibilidadPort.findTurnosOcupados(any(), any(), any())).thenReturn(List.of());

        AvailabilityResponse response = service.consultar(request(sillonId));

        assertThat(response.franjas()).extracting(franja -> franja.inicio().toLocalTime())
            .containsExactly(LocalTime.of(9, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void turnosCanceladosNoOcupanLaAgenda() {
        stubProfesionalYPrestacion(true);
        when(horarioAtencionRepository.findByProfesionalId(profesionalId))
            .thenReturn(List.of(horario(profesionalId, LocalTime.of(9, 0), LocalTime.of(12, 0))));
        when(bloqueoRepository.findByProfesionalId(profesionalId)).thenReturn(List.of());
        when(turnoDisponibilidadPort.findTurnosOcupados(any(), any(), any())).thenReturn(List.of(
            new Appointment(LocalDateTime.of(FECHA, LocalTime.of(9, 30)),
                LocalDateTime.of(FECHA, LocalTime.of(10, 0)), EstadoTurno.CANCELADO)));

        AvailabilityResponse response = service.consultar(request(null));

        assertThat(response.franjas()).hasSize(6);
    }

    private void stubProfesionalYPrestacion(boolean profesionalActivo) {
        when(profesionalRepository.findById(profesionalId))
            .thenReturn(Optional.of(profesional(profesionalId, profesionalActivo)));
        Prestacion prestacion = new Prestacion();
        prestacion.setDuracionMinutos(30);
        prestacion.setActivo(true);
        when(prestacionRepository.findById(prestacionId)).thenReturn(Optional.of(prestacion));
    }

    private AvailabilityRequest request(UUID sillonId) {
        return new AvailabilityRequest(profesionalId, FECHA, prestacionId, sillonId);
    }

    private Profesional profesional(UUID id, boolean activo) {
        Profesional profesional = new Profesional();
        profesional.setId(id);
        profesional.setActivo(activo);
        profesional.setNombreCompleto("Dr. Demo Ficticio");
        return profesional;
    }

    private SillonBox sillonBox(UUID id, boolean activo) {
        SillonBox sillon = new SillonBox();
        sillon.setId(id);
        sillon.setActivo(activo);
        sillon.setNombre("Sillón 1");
        return sillon;
    }

    private HorarioAtencion horario(UUID id, LocalTime inicio, LocalTime fin) {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional(id, true));
        horario.setDiaSemana(FECHA.getDayOfWeek().getValue());
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horario.setActivo(true);
        return horario;
    }

    private Bloqueo bloqueo(UUID profesionalId, UUID sillonId, LocalTime inicio, LocalTime fin) {
        Bloqueo bloqueo = new Bloqueo();
        if (profesionalId != null) {
            bloqueo.setProfesional(profesional(profesionalId, true));
        }
        if (sillonId != null) {
            bloqueo.setSillonBox(sillonBox(sillonId, true));
        }
        bloqueo.setFechaHoraInicio(LocalDateTime.of(FECHA, inicio));
        bloqueo.setFechaHoraFin(LocalDateTime.of(FECHA, fin));
        bloqueo.setActivo(true);
        return bloqueo;
    }
}
