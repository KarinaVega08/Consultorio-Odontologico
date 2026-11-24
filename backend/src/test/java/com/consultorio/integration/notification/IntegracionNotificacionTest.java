package com.consultorio.integration.notification;

import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.application.service.TurnoService;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.Turno;
import com.consultorio.domain.repository.TurnoRepository;
import com.consultorio.notification.application.event.AsistenciaConfirmadaEvent;
import com.consultorio.notification.application.event.TurnoCanceladoEvent;
import com.consultorio.notification.application.event.TurnoCanceladoPorPacienteEvent;
import com.consultorio.notification.application.event.TurnoCreadoEvent;
import com.consultorio.notification.application.event.TurnoReprogramadoEvent;
import com.consultorio.notification.domain.model.TurnoNotificable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests del pegamento C-05 ↔ C-06. Todos los datos son ficticios (Ley 25.326). */
@ExtendWith(MockitoExtension.class)
class IntegracionNotificacionTest {

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TurnoRepository turnoRepository;
    @Mock private TurnoService turnoService;

    private TurnoNotificableMapper mapper;
    private TurnosNotificacionPublisher publisher;
    private TurnoRecordatorioAdapter adapter;
    private TurnoEstadoPorNotificacionListener listener;

    private Turno turno;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        mapper = new TurnoNotificableMapper();
        publisher = new TurnosNotificacionPublisher(eventPublisher, mapper);
        adapter = new TurnoRecordatorioAdapter(turnoRepository, mapper);
        listener = new TurnoEstadoPorNotificacionListener(turnoService);

        inicio = LocalDateTime.of(2026, 11, 25, 9, 30);

        Paciente paciente = new Paciente();
        paciente.setId(UUID.randomUUID());
        paciente.setNombreCompleto("Juan Pérez (paciente ficticio)");
        paciente.setDni("99999999");
        paciente.setEmailRecuperacion("paciente.demo@gmail.com");

        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dra. Ana Demo");

        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Consulta general");

        turno = new Turno();
        turno.setId(UUID.randomUUID());
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setPrestacion(prestacion);
        turno.setFechaHoraInicio(inicio);
        turno.setEstado(EstadoTurno.RESERVADO);
    }

    @Test
    void mapperConstruyeTurnoNotificableCompleto() {
        TurnoNotificable nota = mapper.toNotificable(turno);

        assertEquals(turno.getId(), nota.turnoId());
        assertEquals("99999999", nota.dniPaciente());
        assertEquals("paciente.demo@gmail.com", nota.emailPaciente());
        assertEquals("Dra. Ana Demo", nota.profesionalNombre());
        assertEquals(inicio, nota.fechaHoraInicio());
        assertEquals("Consulta general", nota.prestacionNombre());
    }

    @Test
    void creacionPublicaEventoDeConfirmacion() {
        publisher.publicarCreado(turno);

        ArgumentCaptor<TurnoCreadoEvent> captor = ArgumentCaptor.forClass(TurnoCreadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(turno.getId(), captor.getValue().turno().turnoId());
    }

    @Test
    void pacienteSinCorreoNoPublicaNingunEvento() {
        turno.getPaciente().setEmailRecuperacion(null);

        publisher.publicarCreado(turno);

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reprogramacionPublicaEventoConFechaAnterior() {
        LocalDateTime anterior = inicio.minusDays(1);

        publisher.publicarReprogramado(turno, anterior);

        ArgumentCaptor<TurnoReprogramadoEvent> captor = ArgumentCaptor.forClass(TurnoReprogramadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(anterior, captor.getValue().fechaHoraAnterior());
    }

    @Test
    void cancelacionPublicaEvento() {
        publisher.publicarCancelado(turno);

        ArgumentCaptor<TurnoCanceladoEvent> captor = ArgumentCaptor.forClass(TurnoCanceladoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(turno.getId(), captor.getValue().turno().turnoId());
    }

    @Test
    void adaptadorMapeaLosTurnosDeLaVentana() {
        LocalDateTime desde = inicio.minusHours(1);
        LocalDateTime hasta = inicio.plusHours(2);
        when(turnoRepository.findRecordatoriosEnVentana(desde, hasta)).thenReturn(List.of(turno));

        List<TurnoNotificable> resultado = adapter.findTurnosEnVentana(desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals("paciente.demo@gmail.com", resultado.get(0).emailPaciente());
    }

    @Test
    void listenerDeEnlacesDelegaEnElServicio() {
        UUID turnoId = turno.getId();
        LocalDateTime ahora = LocalDateTime.now();

        listener.onTurnoCanceladoPorPaciente(new TurnoCanceladoPorPacienteEvent(turnoId, ahora));
        listener.onAsistenciaConfirmada(new AsistenciaConfirmadaEvent(turnoId, ahora));

        verify(turnoService).cancelarDesdeEnlace(turnoId, ahora);
        verify(turnoService).confirmarDesdeEnlace(turnoId, ahora);
    }
}
