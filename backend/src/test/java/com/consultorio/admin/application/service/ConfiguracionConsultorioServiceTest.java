package com.consultorio.admin.application.service;

import com.consultorio.admin.application.dto.BloqueoRequest;
import com.consultorio.admin.application.dto.HorarioAtencionRequest;
import com.consultorio.admin.application.dto.PrestacionRequest;
import com.consultorio.admin.application.dto.ProfesionalResponse;
import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.domain.exception.BusinessException;
import com.consultorio.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionConsultorioServiceTest {

    @Mock
    private ProfesionalRepository profesionalRepository;
    @Mock
    private PrestacionRepository prestacionRepository;
    @Mock
    private HorarioAtencionRepository horarioAtencionRepository;
    @Mock
    private BloqueoRepository bloqueoRepository;

    @InjectMocks
    private ConfiguracionConsultorioService service;

    private UUID profesionalId;

    @BeforeEach
    void setUp() {
        profesionalId = UUID.randomUUID();
    }

    @Test
    void crearPrestacionConDuracionValidaLaPersisteActiva() {
        when(prestacionRepository.save(any(Prestacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.crearPrestacion(new PrestacionRequest("Obturación", 45, "Arreglo ficticio"));

        ArgumentCaptor<Prestacion> captor = ArgumentCaptor.forClass(Prestacion.class);
        verify(prestacionRepository).save(captor.capture());
        assertThat(captor.getValue().getDuracionMinutos()).isEqualTo(45);
        assertThat(captor.getValue().getActivo()).isTrue();
        assertThat(respuesta.duracionMinutos()).isEqualTo(45);
    }

    @Test
    void crearPrestacionConDuracionCeroLanzaBusinessException() {
        assertThatThrownBy(() -> service.crearPrestacion(new PrestacionRequest("Inválida", 0, null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("duración");

        verify(prestacionRepository, never()).save(any());
    }

    @Test
    void crearHorarioRechazaDiaFueraDeRangoYHorarioInvertido() {
        assertThatThrownBy(() -> service.crearHorario(new HorarioAtencionRequest(
            profesionalId, 8, LocalTime.of(9, 0), LocalTime.of(17, 0))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("día");

        assertThatThrownBy(() -> service.crearHorario(new HorarioAtencionRequest(
            profesionalId, 1, LocalTime.of(17, 0), LocalTime.of(9, 0))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("hora");
    }

    @Test
    void crearHorarioConProfesionalInexistenteLanzaNotFound() {
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearHorario(new HorarioAtencionRequest(
            profesionalId, 1, LocalTime.of(9, 0), LocalTime.of(17, 0))))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(horarioAtencionRepository, never()).save(any());
    }

    @Test
    void crearBloqueoRequiereAlMenosUnaReferenciaYFechasValidas() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime fin = inicio.plusHours(1);

        assertThatThrownBy(() -> service.crearBloqueo(new BloqueoRequest(
            null, null, inicio, fin, "Sin referencia")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("profesional o un sillón");

        assertThatThrownBy(() -> service.crearBloqueo(new BloqueoRequest(
            profesionalId, null, fin, inicio, "Invertido")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("fecha");
    }

    @Test
    void crearBloqueoSoloProfesionalSeAcepta() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime fin = inicio.plusHours(1);
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesionalFicticio()));
        when(bloqueoRepository.save(any(Bloqueo.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.crearBloqueo(new BloqueoRequest(
            profesionalId, null, inicio, fin, "Capacitación"));

        assertThat(respuesta.profesionalId()).isEqualTo(profesionalId);
        assertThat(respuesta.sillonBoxId()).isNull();
    }

    @Test
    void desactivarProfesionalHaceBajaLogicaSinBorrar() {
        Profesional profesional = profesionalFicticio();
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesional));
        when(profesionalRepository.save(any(Profesional.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfesionalResponse respuesta = service.activarProfesional(profesionalId, false);

        assertThat(respuesta.activo()).isFalse();
        assertThat(profesional.getActivo()).isFalse();
        verify(profesionalRepository).save(profesional);
    }

    private Profesional profesionalFicticio() {
        Profesional profesional = new Profesional();
        profesional.setId(profesionalId);
        profesional.setNombreCompleto("Dra. Ana Ficticia");
        profesional.setMatricula("MP-99999");
        profesional.setActivo(true);
        return profesional;
    }
}
