package com.consultorio.api.controller;

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
import com.consultorio.application.dto.AvailabilityResponse;
import com.consultorio.application.dto.AvailabilitySlotResponse;
import com.consultorio.domain.availability.Appointment;
import com.consultorio.domain.availability.TurnoDisponibilidadPort;
import com.consultorio.domain.model.EstadoTurno;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AvailabilityControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 25);

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProfesionalRepository profesionalRepository;
    @Autowired private PrestacionRepository prestacionRepository;
    @Autowired private SillonBoxRepository sillonBoxRepository;
    @Autowired private HorarioAtencionRepository horarioAtencionRepository;
    @Autowired private BloqueoRepository bloqueoRepository;
    @Autowired private TurnosFicticiosPort turnosFicticiosPort;

    private UUID profesionalId;
    private UUID prestacionId;
    private UUID sillonId;

    @TestConfiguration
    static class TurnosFicticiosConfig {
        @Bean
        @Primary
        TurnosFicticiosPort turnosFicticiosPort() {
            return new TurnosFicticiosPort();
        }
    }

    static class TurnosFicticiosPort implements TurnoDisponibilidadPort {
        private List<Appointment> turnos = new ArrayList<>();

        void usar(List<Appointment> turnos) {
            this.turnos = turnos;
        }

        @Override
        public List<Appointment> findTurnosOcupados(UUID profesionalId, LocalDate fecha, UUID sillonBoxId) {
            return turnos;
        }
    }

    @BeforeEach
    void setUp() {
        bloqueoRepository.deleteAll();
        horarioAtencionRepository.deleteAll();
        prestacionRepository.deleteAll();
        sillonBoxRepository.deleteAll();
        profesionalRepository.deleteAll();
        turnosFicticiosPort.usar(List.of());

        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dr. Demo Ficticio");
        profesional.setMatricula("MP-99999");
        profesionalId = profesionalRepository.save(profesional).getId();

        Prestacion prestacion = new Prestacion();
        prestacion.setNombre("Consulta");
        prestacion.setDuracionMinutos(30);
        prestacionId = prestacionRepository.save(prestacion).getId();

        SillonBox sillon = new SillonBox();
        sillon.setNombre("Sillón 1");
        sillonId = sillonBoxRepository.save(sillon).getId();

        guardarHorario(LocalTime.of(9, 0), LocalTime.of(12, 0));
    }

    @Test
    void escenario1_franjasLibresSinAutenticacion() throws Exception {
        AvailabilityResponse response = consultar(profesionalId, prestacionId, null);

        assertThat(response.duracionMinutos()).isEqualTo(30);
        assertThat(horas(response)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
            LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void escenario2_sinHorarioDevuelveListaVacia() throws Exception {
        horarioAtencionRepository.deleteAll();

        AvailabilityResponse response = consultar(profesionalId, prestacionId, null);

        assertThat(response.franjas()).isEmpty();
    }

    @Test
    void escenario3_turnoBordeABorde() throws Exception {
        turnosFicticiosPort.usar(List.of(new Appointment(
            LocalDateTime.of(FECHA, LocalTime.of(9, 30)),
            LocalDateTime.of(FECHA, LocalTime.of(10, 0)),
            EstadoTurno.CONFIRMADO)));

        AvailabilityResponse response = consultar(profesionalId, prestacionId, null);

        assertThat(horas(response)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void escenario4_bloqueoDeSillon() throws Exception {
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setSillonBox(sillonBoxRepository.findById(sillonId).orElseThrow());
        bloqueo.setFechaHoraInicio(LocalDateTime.of(FECHA, LocalTime.of(10, 0)));
        bloqueo.setFechaHoraFin(LocalDateTime.of(FECHA, LocalTime.of(11, 0)));
        bloqueoRepository.save(bloqueo);

        AvailabilityResponse response = consultar(profesionalId, prestacionId, sillonId);

        assertThat(horas(response)).containsExactly(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    void escenario5_prestacionNoCabeEnElHorario() throws Exception {
        horarioAtencionRepository.deleteAll();
        guardarHorario(LocalTime.of(9, 0), LocalTime.of(9, 20));

        AvailabilityResponse response = consultar(profesionalId, prestacionId, null);

        assertThat(response.franjas()).isEmpty();
    }

    @Test
    void parametrosFaltantesDevuelven400() throws Exception {
        mockMvc.perform(get("/api/availability")
                .param("fecha", FECHA.toString())
                .param("prestacionId", prestacionId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.profesionalId").exists());
    }

    @Test
    void fechaConFormatoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/availability")
                .param("profesionalId", profesionalId.toString())
                .param("fecha", "25-11-2026")
                .param("prestacionId", prestacionId.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void profesionalInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/availability")
                .param("profesionalId", UUID.randomUUID().toString())
                .param("fecha", FECHA.toString())
                .param("prestacionId", prestacionId.toString()))
            .andExpect(status().isNotFound());
    }

    private AvailabilityResponse consultar(UUID profesional, UUID prestacion, UUID sillon) throws Exception {
        MockHttpServletRequestBuilder peticion = get("/api/availability")
            .param("profesionalId", profesional.toString())
            .param("fecha", FECHA.toString())
            .param("prestacionId", prestacion.toString());
        if (sillon != null) {
            peticion.param("sillonBoxId", sillon.toString());
        }

        String cuerpo = mockMvc.perform(peticion)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(cuerpo, AvailabilityResponse.class);
    }

    private void guardarHorario(LocalTime inicio, LocalTime fin) {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesionalRepository.findById(profesionalId).orElseThrow());
        horario.setDiaSemana(FECHA.getDayOfWeek().getValue());
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horarioAtencionRepository.save(horario);
    }

    private List<LocalTime> horas(AvailabilityResponse response) {
        return response.franjas().stream()
            .map(AvailabilitySlotResponse::inicio)
            .map(LocalDateTime::toLocalTime)
            .toList();
    }
}
