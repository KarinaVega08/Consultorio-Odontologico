package com.consultorio.api.controller;

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
import com.consultorio.application.service.TokenService;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.RolUsuario;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.TurnoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escenarios extremo a extremo de C-05 sobre H2 en memoria, sin servicios externos.
 * Todos los datos son ficticios (Ley 25.326).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentIntegrationTest {

    private static final LocalDate FECHA = LocalDate.of(2099, 11, 25);

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TokenService tokenService;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private BloqueoRepository bloqueoRepository;
    @Autowired private HorarioAtencionRepository horarioAtencionRepository;
    @Autowired private PrestacionRepository prestacionRepository;
    @Autowired private SillonBoxRepository sillonBoxRepository;
    @Autowired private ProfesionalRepository profesionalRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private Profesional profesional;
    private SillonBox sillonBox;
    private Prestacion prestacion;
    private Paciente pacienteA;
    private Paciente pacienteB;
    private String tokenRecepcionista;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
        bloqueoRepository.deleteAll();
        horarioAtencionRepository.deleteAll();
        prestacionRepository.deleteAll();
        sillonBoxRepository.deleteAll();
        profesionalRepository.deleteAll();
        pacienteRepository.deleteAll();

        profesional = profesionalRepository.save(profesional());
        sillonBox = sillonBoxRepository.save(sillon());
        prestacion = prestacionRepository.save(prestacion());
        pacienteA = pacienteRepository.save(paciente("99999999", "Paciente Ficticio A"));
        pacienteB = pacienteRepository.save(paciente("99999998", "Paciente Ficticio B"));

        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(FECHA.getDayOfWeek().getValue());
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(17, 0));
        horarioAtencionRepository.save(horario);

        tokenRecepcionista = bearer(RolUsuario.RECEPCIONISTA, UUID.randomUUID(), "99999990");
    }

    @Test
    void cicloCrearBordeCancelarYLiberarHueco() throws Exception {
        String id09 = crearTurno(pacienteA.getId(), "09:00:00");

        // Borde a borde: uno empieza exactamente cuando termina el anterior.
        crearTurno(pacienteA.getId(), "09:30:00");

        // Solape real: rechazado con el mensaje de negocio exacto.
        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(pacienteA.getId(), "09:15:00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El profesional o el sillón ya tiene un turno en ese horario"));

        // Cancelación lógica: sigue consultable y libera el hueco.
        mockMvc.perform(delete("/api/appointments/{id}", id09)
                .header("Authorization", tokenRecepcionista))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(get("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .param("profesionalId", profesional.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + id09 + "')].estado").value("CANCELADO"));

        crearTurno(pacienteA.getId(), "09:00:00");

        mockMvc.perform(delete("/api/appointments/{id}", id09)
                .header("Authorization", tokenRecepcionista))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El turno ya se encuentra cancelado"));
    }

    @Test
    void mismoProfesionalNoPuedeAtenderDosSillonesEnElMismoHorario() throws Exception {
        SillonBox sillon2 = sillonBoxRepository.save(sillon("Sillón 2"));
        crearTurno(pacienteA.getId(), "09:00:00");

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(pacienteB.getId(), "09:00:00", sillon2.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El profesional o el sillón ya tiene un turno en ese horario"));

        String cuerpo = mockMvc.perform(get("/api/availability")
                .param("profesionalId", profesional.getId().toString())
                .param("fecha", FECHA.toString())
                .param("prestacionId", prestacion.getId().toString())
                .param("sillonBoxId", sillon2.getId().toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        AvailabilityResponse disponibilidad = objectMapper.readValue(cuerpo, AvailabilityResponse.class);

        assertThat(disponibilidad.franjas()).extracting(AvailabilitySlotResponse::inicio)
            .extracting(LocalDateTime::toLocalTime)
            .doesNotContain(LocalTime.of(9, 0))
            .contains(LocalTime.of(9, 30));

        // Borde a borde en otro sillón: el profesional ya terminó a las 09:30.
        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(pacienteB.getId(), "09:30:00", sillon2.getId())))
            .andExpect(status().isCreated());
    }

    @Test
    void reprogramacionLiberaElHuecoAnteriorYRevalidaSolapes() throws Exception {
        String id10 = crearTurno(pacienteA.getId(), "10:00:00");

        mockMvc.perform(put("/api/appointments/{id}", id10)
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaHoraInicio\":\"2099-11-25T14:00:00\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fechaHoraInicio").value("2099-11-25T14:00:00"))
            .andExpect(jsonPath("$.fechaHoraFin").value("2099-11-25T14:30:00"));

        crearTurno(pacienteA.getId(), "10:00:00");

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(pacienteA.getId(), "14:15:00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El profesional o el sillón ya tiene un turno en ese horario"));
    }

    @Test
    void pacienteAjenoNoPuedeCancelarTurnoDeOtroPaciente() throws Exception {
        String idTurnoB = crearTurno(pacienteB.getId(), "11:00:00");
        String tokenPacienteA = bearer(RolUsuario.PACIENTE, pacienteA.getId(), "99999999");

        mockMvc.perform(delete("/api/appointments/{id}", idTurnoB)
                .header("Authorization", tokenPacienteA))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .param("profesionalId", profesional.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + idTurnoB + "')].estado").value("RESERVADO"));
    }

    private String crearTurno(UUID pacienteId, String hora) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/appointments")
                .header("Authorization", tokenRecepcionista)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(pacienteId, hora)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("RESERVADO"))
            .andExpect(jsonPath("$.fechaHoraFin").value("2099-11-25T" + horaFin(hora)))
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private static String horaFin(String hora) {
        String[] partes = hora.split(":");
        int minutos = Integer.parseInt(partes[1]) + 30;
        int horaFin = Integer.parseInt(partes[0]) + (minutos / 60);
        return String.format("%02d:%02d:00", horaFin, minutos % 60);
    }

    private String payload(UUID pacienteId, String hora) {
        return payload(pacienteId, hora, sillonBox.getId());
    }

    private String payload(UUID pacienteId, String hora, UUID sillonId) {
        return """
            {
              "pacienteId": "%s",
              "profesionalId": "%s",
              "sillonBoxId": "%s",
              "prestacionId": "%s",
              "fechaHoraInicio": "2099-11-25T%s"
            }
            """.formatted(pacienteId, profesional.getId(), sillonId, prestacion.getId(), hora);
    }

    private String bearer(RolUsuario rol, UUID userId, String dni) {
        return "Bearer " + tokenService.generateToken(userId, dni, rol);
    }

    private static Profesional profesional() {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        profesional.setMatricula("MP-99999");
        return profesional;
    }

    private static SillonBox sillon() {
        return sillon("Sillón 1");
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

    private static Paciente paciente(String dni, String nombre) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombreCompleto(nombre);
        paciente.setPasswordHash("hash-ficticio");
        return paciente;
    }
}
