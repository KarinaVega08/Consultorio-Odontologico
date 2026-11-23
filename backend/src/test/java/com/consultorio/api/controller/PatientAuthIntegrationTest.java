package com.consultorio.api.controller;

import com.consultorio.domain.model.RecoveryToken;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.RecoveryTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para todos los escenarios del spec C-02 auth-pacientes.
 * Usa exclusivamente datos ficticios (Ley 25.326) y H2 en memoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PatientAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private RecoveryTokenRepository recoveryTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();
    }

    @Test
    void scenario1_registerAndLoginShouldSucceedAndInviteGmailLink() throws Exception {
        String registerPayload = """
                {
                  "nombreCompleto": "Juan Pérez",
                  "dni": "99999991",
                  "password": "ClaveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value("99999991"))
                .andExpect(jsonPath("$.requiereVincularGmail").value(true))
                .andExpect(content().string(not(containsString("passwordHash"))));

        String loginPayload = """
                {
                  "dni": "99999991",
                  "password": "ClaveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.patient.requiereVincularGmail").value(true));
    }

    @Test
    void scenario2_registerWithDuplicateDniShouldReturnBadRequest() throws Exception {
        String payload = """
                {
                  "nombreCompleto": "Paciente Ficticio",
                  "dni": "99999992",
                  "password": "ClaveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe una cuenta con este DNI"));
    }

    @Test
    void scenario3_registerWithInvalidDniShouldReturnValidationErrors() throws Exception {
        String shortDni = """
                {
                  "nombreCompleto": "Paciente Ficticio",
                  "dni": "123",
                  "password": "ClaveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shortDni))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.dni").exists());

        String alphaDni = """
                {
                  "nombreCompleto": "Paciente Ficticio",
                  "dni": "1234ABCD",
                  "password": "ClaveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alphaDni))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.dni").exists());
    }

    @Test
    void scenario4_loginWithWrongPasswordShouldReturnUnauthorized() throws Exception {
        String registerPayload = """
                {
                  "nombreCompleto": "Paciente Tres",
                  "dni": "99999993",
                  "password": "ClaveValida1"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        String wrongLogin = """
                {
                  "dni": "99999993",
                  "password": "Incorrecta"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongLogin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scenario5_linkRecoveryEmailShouldWorkOnceAndRejectSecondAttempt() throws Exception {
        String registerPayload = """
                {
                  "nombreCompleto": "Paciente Cuatro",
                  "dni": "99999994",
                  "password": "ClaveValida1"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999994\",\"password\":\"ClaveValida1\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        mockMvc.perform(post("/api/patients/me/recovery-email")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"juan.perez.test@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailRecuperacion").value("juan.perez.test@gmail.com"))
                .andExpect(jsonPath("$.requiereVincularGmail").value(false));

        mockMvc.perform(post("/api/patients/me/recovery-email")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"otro.test@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo de recuperación ya fue vinculado y no puede modificarse"));
    }

    @Test
    void scenario6_protectedPatientEndpointsShouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/api/patients/me/recovery-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"juan.perez.test@gmail.com\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void staticLoginViewShouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk());
    }

    @Test
    void scenario7_recoverPasswordWithLinkedGmailReturnsOk() throws Exception {
        // Registrar paciente con Gmail vinculado
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Paciente Seis",
                                  "dni": "99999996",
                                  "password": "ClaveSegura123"
                                }
                                """))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999996\",\"password\":\"ClaveSegura123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String sessionToken = loginJson.get("token").asText();

        // Vincular Gmail
        mockMvc.perform(post("/api/patients/me/recovery-email")
                        .header("Authorization", "Bearer " + sessionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"paciente.seis.test@gmail.com\"}"))
                .andExpect(status().isOk());

        // Solicitar recuperación de contraseña (debe devolver 200 aunque no exponga el token)
        mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999996\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void scenario8_recoverPasswordWithoutLinkedGmailStillReturnsOk() throws Exception {
        // Registrar paciente SIN Gmail vinculado
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Paciente Siete",
                                  "dni": "99999997",
                                  "password": "ClaveSegura123"
                                }
                                """))
                .andExpect(status().isCreated());

        // Solicitar recuperación sin Gmail vinculado (debe devolver 200, no filtrar)
        mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999997\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void scenario9_resetPasswordWithValidTokenWorks() throws Exception {
        // Registrar paciente
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Paciente Ocho",
                                  "dni": "99999998",
                                  "password": "ClaveVieja123"
                                }
                                """))
                .andExpect(status().isCreated());

        // Crear token de recuperación directamente en la BD
        RecoveryToken token = new RecoveryToken();
        token.setToken("token-recuperacion-valido-1234567890");
        token.setDni("99999998");
        token.setFechaExpiracion(LocalDateTime.now().plusHours(1));
        token.setUsado(false);
        token.setActivo(true);
        recoveryTokenRepository.save(token);

        // Resetear contraseña con el token
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-recuperacion-valido-1234567890\",\"newPassword\":\"ClaveNueva123\"}"))
                .andExpect(status().isOk());

        // Verificar que se puede hacer login con la nueva contraseña
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999998\",\"password\":\"ClaveNueva123\"}"))
                .andExpect(status().isOk());

        // Verificar que la contraseña vieja ya no funciona
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999998\",\"password\":\"ClaveVieja123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scenario10_resetPasswordWithInvalidTokenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-inexistente\",\"newPassword\":\"ClaveNueva123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void scenario11_adminPasswordResetRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/admin/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"99999991\",\"newPassword\":\"NuevaClave123\"}"))
                .andExpect(status().isForbidden());
    }
}
