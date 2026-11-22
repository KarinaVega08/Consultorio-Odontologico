package com.consultorio.notification.application.service;

import com.consultorio.domain.exception.BusinessException;
import com.consultorio.notification.application.event.TurnoCreadoEvent;
import com.consultorio.notification.application.port.EmailSenderPort;
import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import com.consultorio.notification.domain.model.ConfiguracionNotificacion;
import com.consultorio.notification.domain.model.NotificacionToken;
import com.consultorio.notification.domain.model.TipoNotificacionToken;
import com.consultorio.notification.domain.model.TurnoNotificable;
import com.consultorio.notification.domain.repository.ConfiguracionNotificacionRepository;
import com.consultorio.notification.domain.repository.NotificacionTokenRepository;
import com.consultorio.notification.infrastructure.email.EmailDeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionTokenRepository tokenRepository;
    @Mock
    private ConfiguracionNotificacionRepository configuracionRepository;
    @Mock
    private EmailSenderPort emailSender;
    @Mock
    private TurnoParaRecordarPort turnoParaRecordarPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificacionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "horasPorDefecto", 24);
        ReflectionTestUtils.setField(service, "validezHoras", 48L);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
    }

    @Test
    void crearTurnoEnviaConfirmacionConDosEnlacesUnicos() {
        when(tokenRepository.save(any(NotificacionToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onTurnoCreado(new TurnoCreadoEvent(turnoFicticio()));

        ArgumentCaptor<NotificacionToken> captor = ArgumentCaptor.forClass(NotificacionToken.class);
        verify(tokenRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(NotificacionToken::getTipo)
            .containsExactlyInAnyOrder(
                TipoNotificacionToken.CONFIRMAR_ASISTENCIA,
                TipoNotificacionToken.CANCELAR_TURNO
            );
        assertThat(captor.getAllValues()).allSatisfy(token -> {
            assertThat(token.getUsado()).isFalse();
            assertThat(token.getFechaExpiracion()).isAfter(LocalDateTime.now());
            assertThat(token.getToken()).doesNotContain("-");
        });
        verify(emailSender).enviar(
            eq("paciente.ficticio@example.com"),
            contains("confirmación"),
            contains("http://localhost:8080/api/notifications/confirm?token=")
        );
    }

    @Test
    void falloDeSmtpNoSePropagaAlFlujoDeTurnos() {
        doThrow(new EmailDeliveryException("SMTP caído", new RuntimeException("connection refused")))
            .when(emailSender).enviar(any(), any(), any());

        assertThatCode(() -> service.onTurnoCreado(new TurnoCreadoEvent(turnoFicticio())))
            .doesNotThrowAnyException();
    }

    @Test
    void recordatorioUsaLaAntelacionPorDefectoYNoSeDuplica() {
        when(configuracionRepository.findFirstByActivoTrue()).thenReturn(Optional.empty());
        when(turnoParaRecordarPort.findTurnosEnVentana(any(), any())).thenReturn(List.of(turnoFicticio()));
        when(tokenRepository.existsByTurnoIdAndTipoAndUsadoFalse(
            any(), eq(TipoNotificacionToken.RECORDATORIO))).thenReturn(false, true);
        when(tokenRepository.save(any(NotificacionToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.enviarRecordatorios();
        service.enviarRecordatorios();

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hasta = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(turnoParaRecordarPort, times(2)).findTurnosEnVentana(desde.capture(), hasta.capture());
        assertThat(java.time.Duration.between(desde.getValue(), hasta.getValue()).toHours()).isEqualTo(24);
        verify(emailSender, times(1)).enviar(eq("paciente.ficticio@example.com"), contains("recordatorio"), any());
    }

    @Test
    void confirmarAsistenciaMarcaTokenUsadoYRechazaReutilizacion() {
        NotificacionToken valido = token(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, true, LocalDateTime.now().plusDays(1));
        when(tokenRepository.findByToken("valido")).thenReturn(Optional.of(valido));

        service.confirmarAsistencia("valido");

        assertThat(valido.getUsado()).isTrue();
        verify(tokenRepository).save(valido);
        verify(eventPublisher).publishEvent(any(Object.class));

        NotificacionToken usado = token(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, true, LocalDateTime.now().plusDays(1));
        usado.setUsado(true);
        when(tokenRepository.findByToken("usado")).thenReturn(Optional.of(usado));
        assertThatThrownBy(() -> service.confirmarAsistencia("usado"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("ya fue utilizado");

        NotificacionToken vencido = token(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, true, LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken("vencido")).thenReturn(Optional.of(vencido));
        assertThatThrownBy(() -> service.confirmarAsistencia("vencido"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("vencido");
    }

    @Test
    void cancelarConTokenPublicaEventoParaC05() {
        NotificacionToken token = token(TipoNotificacionToken.CANCELAR_TURNO, true, LocalDateTime.now().plusDays(1));
        when(tokenRepository.findByToken("cancelar")).thenReturn(Optional.of(token));

        service.cancelarTurnoConToken("cancelar");

        assertThat(token.getUsado()).isTrue();
        verify(tokenRepository).save(token);
        verify(eventPublisher).publishEvent(any(com.consultorio.notification.application.event.TurnoCanceladoPorPacienteEvent.class));
    }

    @Test
    void tokenInexistenteEsRechazado() {
        when(tokenRepository.findByToken("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmarAsistencia("inexistente"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("no es válido");
        verify(emailSender, never()).enviar(any(), any(), any());
    }

    @Test
    void cambiarAntelacionLaPersiste() {
        when(configuracionRepository.findFirstByActivoTrue()).thenReturn(Optional.empty());
        when(configuracionRepository.save(any(ConfiguracionNotificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        service.actualizarHorasAntelacion(48);

        ArgumentCaptor<ConfiguracionNotificacion> captor = ArgumentCaptor.forClass(ConfiguracionNotificacion.class);
        verify(configuracionRepository).save(captor.capture());
        assertThat(captor.getValue().getHorasAntelacion()).isEqualTo(48);
    }

    @Test
    void antelacionCeroEsRechazada() {
        assertThatThrownBy(() -> service.actualizarHorasAntelacion(0))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("mayor a 0");
    }

    private TurnoNotificable turnoFicticio() {
        return new TurnoNotificable(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "99.999.999",
            "paciente.ficticio@example.com",
            "Dra. Ana Ficticia",
            LocalDateTime.now().plusHours(20),
            "Consulta/Diagnóstico"
        );
    }

    private NotificacionToken token(TipoNotificacionToken tipo, boolean activo, LocalDateTime expiracion) {
        NotificacionToken token = new NotificacionToken();
        token.setToken(tipo == TipoNotificacionToken.CANCELAR_TURNO ? "cancelar" : "valido");
        token.setTipo(tipo);
        token.setTurnoId(UUID.randomUUID());
        token.setEmailDestino("paciente.ficticio@example.com");
        token.setFechaExpiracion(expiracion);
        token.setUsado(false);
        token.setActivo(activo);
        return token;
    }
}
