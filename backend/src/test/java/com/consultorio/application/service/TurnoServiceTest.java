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
import com.consultorio.application.dto.AppointmentResponse;
import com.consultorio.application.dto.CreateAppointmentRequest;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RolUsuario;
import org.springframework.security.access.AccessDeniedException;
import com.consultorio.domain.model.Turno;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del servicio de turnos (< 50 ms, sin Spring ni BD).
 */
@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 25); // miércoles
    private static final UUID PACIENTE_ID = UUID.randomUUID();
    private static final UUID PROFESIONAL_ID = UUID.randomUUID();
    private static final UUID SILLON_ID = UUID.randomUUID();
    private static final UUID PRESTACION_ID = UUID.randomUUID();

    @Mock private TurnoRepository turnoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private SillonBoxRepository sillonBoxRepository;
    @Mock private PrestacionRepository prestacionRepository;
    @Mock private HorarioAtencionRepository horarioAtencionRepository;
    @Mock private BloqueoRepository bloqueoRepository;

    @InjectMocks private TurnoService turnoService;

    private TokenService.AuthenticatedUser recepcionista;

    @BeforeEach
    void setUp() {
        recepcionista = new TokenService.AuthenticatedUser(UUID.randomUUID(), "99999999", RolUsuario.RECEPCIONISTA);
    }

    @Test
    void creacionExitosaCalculaFinDesdeLaPrestacionYEstadoReservado() {
        LocalDateTime inicio = FECHA.atTime(9, 0);
        when(profesionalRepository.findByIdForUpdate(PROFESIONAL_ID))
            .thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findByIdForUpdate(SILLON_ID)).thenReturn(Optional.of(sillon()));
        when(pacienteRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(paciente()));
        when(prestacionRepository.findById(PRESTACION_ID)).thenReturn(Optional.of(prestacion(30)));
        when(horarioAtencionRepository.findByProfesionalId(PROFESIONAL_ID))
            .thenReturn(List.of(horario(FECHA, 9, 12)));
        when(bloqueoRepository.findByProfesionalId(PROFESIONAL_ID)).thenReturn(List.of());
        when(bloqueoRepository.findBySillonBoxId(SILLON_ID)).thenReturn(List.of());
        when(turnoRepository.findSolapesPorProfesional(any(), any(), any(), any())).thenReturn(List.of());
        when(turnoRepository.findSolapesPorSillon(any(), any(), any(), any())).thenReturn(List.of());
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = turnoService.crear(request(inicio), recepcionista);

        assertThat(response.estado()).isEqualTo(EstadoTurno.RESERVADO);
        assertThat(response.fechaHoraFin()).isEqualTo(inicio.plusMinutes(30));
        assertThat(response.creadoPor()).isEqualTo("99999999");
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void creacionFueraDelHorarioEsRechazada() {
        LocalDateTime inicio = FECHA.atTime(12, 0);
        when(profesionalRepository.findByIdForUpdate(PROFESIONAL_ID))
            .thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findByIdForUpdate(SILLON_ID)).thenReturn(Optional.of(sillon()));
        when(pacienteRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(paciente()));
        when(prestacionRepository.findById(PRESTACION_ID)).thenReturn(Optional.of(prestacion(30)));
        when(horarioAtencionRepository.findByProfesionalId(PROFESIONAL_ID))
            .thenReturn(List.of(horario(FECHA, 9, 12)));

        assertThatThrownBy(() -> turnoService.crear(request(inicio), recepcionista))
            .isInstanceOf(BusinessException.class)
            .hasMessage("El horario seleccionado no está dentro del horario de atención");

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void creacionConBloqueoDeSillonEsRechazada() {
        LocalDateTime inicio = FECHA.atTime(10, 30);
        when(profesionalRepository.findByIdForUpdate(PROFESIONAL_ID))
            .thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findByIdForUpdate(SILLON_ID)).thenReturn(Optional.of(sillon()));
        when(pacienteRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(paciente()));
        when(prestacionRepository.findById(PRESTACION_ID)).thenReturn(Optional.of(prestacion(30)));
        when(horarioAtencionRepository.findByProfesionalId(PROFESIONAL_ID))
            .thenReturn(List.of(horario(FECHA, 9, 12)));
        when(bloqueoRepository.findByProfesionalId(PROFESIONAL_ID)).thenReturn(List.of());
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setProfesional(profesional());
        bloqueo.setSillonBox(sillon());
        bloqueo.setFechaHoraInicio(FECHA.atTime(10, 0));
        bloqueo.setFechaHoraFin(FECHA.atTime(11, 0));
        when(bloqueoRepository.findBySillonBoxId(SILLON_ID)).thenReturn(List.of(bloqueo));

        assertThatThrownBy(() -> turnoService.crear(request(inicio), recepcionista))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("bloqueado");

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void pacienteAjenoNoPuedeCancelarTurnoDeOtroPaciente() {
        UUID turnoId = UUID.randomUUID();
        Turno existente = turnoReservado(turnoId, 9, 10);
        TokenService.AuthenticatedUser pacienteAjeno =
            new TokenService.AuthenticatedUser(UUID.randomUUID(), "99999997", RolUsuario.PACIENTE);
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> turnoService.cancelar(turnoId, pacienteAjeno))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("El paciente solo puede operar sobre sus propios turnos");

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void pacienteDuenoPuedeCancelarSuPropioTurno() {
        UUID turnoId = UUID.randomUUID();
        Turno existente = turnoReservado(turnoId, 9, 10);
        TokenService.AuthenticatedUser dueno =
            new TokenService.AuthenticatedUser(PACIENTE_ID, "99999999", RolUsuario.PACIENTE);
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(existente));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(turnoService.cancelar(turnoId, dueno).estado()).isEqualTo(EstadoTurno.CANCELADO);
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void listadoFiltraPorProfesionalFechaYEstado() {
        Turno reservado = turnoReservado(UUID.randomUUID(), 9, 10);
        Turno cancelado = turnoReservado(UUID.randomUUID(), 10, 11);
        cancelado.setEstado(EstadoTurno.CANCELADO);
        when(turnoRepository.buscarConFiltros(
            org.mockito.ArgumentMatchers.eq(PROFESIONAL_ID),
            org.mockito.ArgumentMatchers.eq(FECHA.atStartOfDay()),
            org.mockito.ArgumentMatchers.eq(FECHA.atTime(LocalTime.MAX)),
            org.mockito.ArgumentMatchers.eq(EstadoTurno.RESERVADO)))
            .thenReturn(List.of(reservado));

        var response = turnoService.listar(
            new com.consultorio.application.dto.AppointmentQuery(
                PROFESIONAL_ID, FECHA, EstadoTurno.RESERVADO),
            recepcionista);

        assertThat(response).extracting(AppointmentResponse::id).containsExactly(reservado.getId());
        assertThat(response).extracting(AppointmentResponse::estado).containsExactly(EstadoTurno.RESERVADO);
    }

    @Test
    void cancelacionPasaACanceladoSinBorradoFisico() {
        UUID turnoId = UUID.randomUUID();
        Turno existente = turnoReservado(turnoId, 9, 10);
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(existente));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = turnoService.cancelar(turnoId, recepcionista);

        assertThat(response.estado()).isEqualTo(EstadoTurno.CANCELADO);
        verify(turnoRepository).save(any(Turno.class));
        verify(turnoRepository, never()).delete(any(Turno.class));
        verify(turnoRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void cancelacionDeUnTurnoYaCanceladoEsRechazada() {
        UUID turnoId = UUID.randomUUID();
        Turno cancelado = turnoReservado(turnoId, 9, 10);
        cancelado.setEstado(EstadoTurno.CANCELADO);
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> turnoService.cancelar(turnoId, recepcionista))
            .isInstanceOf(BusinessException.class)
            .hasMessage(TurnoService.YA_CANCELADO);

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void reprogramacionLiberaElHuecoAnteriorExcluyendoElPropioTurnoDelSolape() {
        UUID turnoId = UUID.randomUUID();
        Turno existente = turnoReservado(turnoId, 9, 10);
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(existente));
        when(profesionalRepository.findByIdForUpdate(PROFESIONAL_ID))
            .thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findByIdForUpdate(SILLON_ID)).thenReturn(Optional.of(sillon()));
        when(bloqueoRepository.findByProfesionalId(PROFESIONAL_ID)).thenReturn(List.of());
        when(bloqueoRepository.findBySillonBoxId(SILLON_ID)).thenReturn(List.of());
        when(horarioAtencionRepository.findByProfesionalId(PROFESIONAL_ID))
            .thenReturn(List.of(horario(FECHA, 9, 17)));
        when(turnoRepository.findSolapesPorProfesional(any(), any(), any(), any())).thenReturn(List.of());
        when(turnoRepository.findSolapesPorSillon(any(), any(), any(), any())).thenReturn(List.of());
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime nuevoInicio = FECHA.atTime(14, 0);
        AppointmentResponse response = turnoService.reprogramar(
            turnoId, new com.consultorio.application.dto.RescheduleAppointmentRequest(nuevoInicio), recepcionista);

        assertThat(response.fechaHoraInicio()).isEqualTo(nuevoInicio);
        assertThat(response.fechaHoraFin()).isEqualTo(nuevoInicio.plusMinutes(30));
        verify(turnoRepository).findSolapesPorProfesional(
            org.mockito.ArgumentMatchers.eq(PROFESIONAL_ID),
            org.mockito.ArgumentMatchers.eq(nuevoInicio),
            org.mockito.ArgumentMatchers.eq(nuevoInicio.plusMinutes(30)),
            org.mockito.ArgumentMatchers.eq(turnoId));
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void crearEsTransaccionalParaGarantizarLosLocksYElGuard() throws Exception {
        var metodo = TurnoService.class.getMethod(
            "crear", CreateAppointmentRequest.class, TokenService.AuthenticatedUser.class);

        assertThat(metodo.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
            .isTrue();
    }

    @Test
    void solapamientoPorProfesionalEsRechazadoYNoPersiste() {
        stubDependencias();
        when(turnoRepository.findSolapesPorProfesional(any(), any(), any(), any()))
            .thenReturn(List.of(turnoExistente()));

        assertThatThrownBy(() -> turnoService.crear(request(FECHA.atTime(9, 30)), recepcionista))
            .isInstanceOf(BusinessException.class)
            .hasMessage(TurnoService.SOLAPAMIENTO);

        verify(profesionalRepository).findByIdForUpdate(PROFESIONAL_ID);
        verify(sillonBoxRepository).findByIdForUpdate(SILLON_ID);
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void solapamientoPorSillonConOtroProfesionalEsRechazadoYNoPersiste() {
        stubDependencias();
        when(turnoRepository.findSolapesPorProfesional(any(), any(), any(), any())).thenReturn(List.of());
        when(turnoRepository.findSolapesPorSillon(any(), any(), any(), any()))
            .thenReturn(List.of(turnoExistente()));

        assertThatThrownBy(() -> turnoService.crear(request(FECHA.atTime(10, 30)), recepcionista))
            .isInstanceOf(BusinessException.class)
            .hasMessage(TurnoService.SOLAPAMIENTO);

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    private void stubDependencias() {
        when(profesionalRepository.findByIdForUpdate(PROFESIONAL_ID))
            .thenReturn(Optional.of(profesional()));
        when(sillonBoxRepository.findByIdForUpdate(SILLON_ID)).thenReturn(Optional.of(sillon()));
        when(pacienteRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(paciente()));
        when(prestacionRepository.findById(PRESTACION_ID)).thenReturn(Optional.of(prestacion(30)));
        when(horarioAtencionRepository.findByProfesionalId(PROFESIONAL_ID))
            .thenReturn(List.of(horario(FECHA, 9, 12)));
        when(bloqueoRepository.findByProfesionalId(PROFESIONAL_ID)).thenReturn(List.of());
        when(bloqueoRepository.findBySillonBoxId(SILLON_ID)).thenReturn(List.of());
    }

    private static Turno turnoReservado(UUID id, int inicio, int fin) {
        Turno turno = new Turno();
        turno.setId(id);
        turno.setPaciente(paciente());
        turno.setProfesional(profesional());
        turno.setSillonBox(sillon());
        turno.setPrestacion(prestacion(30));
        turno.setFechaHoraInicio(FECHA.atTime(inicio, 0));
        turno.setFechaHoraFin(FECHA.atTime(fin, 0));
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCreadoPor("99999999");
        return turno;
    }

    private static Turno turnoExistente() {
        Turno turno = new Turno();
        turno.setId(UUID.randomUUID());
        turno.setProfesional(profesional());
        turno.setSillonBox(sillon());
        turno.setFechaHoraInicio(FECHA.atTime(9, 0));
        turno.setFechaHoraFin(FECHA.atTime(10, 0));
        turno.setEstado(EstadoTurno.RESERVADO);
        return turno;
    }

    private static CreateAppointmentRequest request(LocalDateTime inicio) {
        return new CreateAppointmentRequest(PACIENTE_ID, PROFESIONAL_ID, SILLON_ID, PRESTACION_ID, inicio);
    }

    private static Paciente paciente() {
        Paciente paciente = new Paciente();
        paciente.setId(PACIENTE_ID);
        paciente.setDni("99999999");
        paciente.setNombreCompleto("Paciente Ficticio");
        return paciente;
    }

    private static Profesional profesional() {
        Profesional profesional = new Profesional();
        profesional.setId(PROFESIONAL_ID);
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        return profesional;
    }

    private static SillonBox sillon() {
        SillonBox sillonBox = new SillonBox();
        sillonBox.setId(SILLON_ID);
        sillonBox.setNombre("Sillón 1");
        return sillonBox;
    }

    private static Prestacion prestacion(int duracion) {
        Prestacion prestacion = new Prestacion();
        prestacion.setId(PRESTACION_ID);
        prestacion.setNombre("Consulta");
        prestacion.setDuracionMinutos(duracion);
        return prestacion;
    }

    private static HorarioAtencion horario(LocalDate fecha, int inicio, int fin) {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional());
        horario.setDiaSemana(fecha.getDayOfWeek().getValue());
        horario.setHoraInicio(LocalTime.of(inicio, 0));
        horario.setHoraFin(LocalTime.of(fin, 0));
        return horario;
    }
}
