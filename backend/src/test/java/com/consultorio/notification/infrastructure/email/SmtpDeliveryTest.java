package com.consultorio.notification.infrastructure.email;

import com.consultorio.notification.application.event.TurnoCreadoEvent;
import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import com.consultorio.notification.domain.model.NotificacionToken;
import com.consultorio.notification.domain.model.TipoNotificacionToken;
import com.consultorio.notification.domain.model.TurnoNotificable;
import com.consultorio.notification.domain.repository.ConfiguracionNotificacionRepository;
import com.consultorio.notification.domain.repository.NotificacionTokenRepository;
import com.consultorio.notification.application.service.NotificacionService;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Entrega real de correo contra un servidor SMTP embebido (equivalente a Mailpit en tests).
 * No requiere servicios externos ni credenciales.
 */
@ExtendWith(MockitoExtension.class)
class SmtpDeliveryTest {

    @RegisterExtension
    final GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup(0, null, "smtp"));

    @Mock private NotificacionTokenRepository tokenRepository;
    @Mock private ConfiguracionNotificacionRepository configuracionRepository;
    @Mock private TurnoParaRecordarPort turnoParaRecordarPort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private NotificacionService service;

    @BeforeEach
    void setUp() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(greenMail.getSmtp().getPort());

        SmtpEmailSender smtp = new SmtpEmailSender(javaMailSender);
        ReflectionTestUtils.setField(smtp, "from", "consultorio@example.com");

        service = new NotificacionService(
            tokenRepository, configuracionRepository, smtp, turnoParaRecordarPort, eventPublisher);
        ReflectionTestUtils.setField(service, "horasPorDefecto", 24);
        ReflectionTestUtils.setField(service, "validezHoras", 48L);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
    }

    @Test
    void recordatorioSeEntregaRealmentePorSmtp() throws Exception {
        when(configuracionRepository.findFirstByActivoTrue()).thenReturn(Optional.empty());
        when(turnoParaRecordarPort.findTurnosEnVentana(any(), any())).thenReturn(List.of(turnoFicticio()));
        when(tokenRepository.existsByTurnoIdAndTipoAndUsadoFalse(
            any(), eq(TipoNotificacionToken.RECORDATORIO))).thenReturn(false);
        when(tokenRepository.save(any(NotificacionToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.enviarRecordatorios();

        MimeMessage[] recibidos = greenMail.getReceivedMessages();
        assertThat(recibidos).hasSize(1);
        assertThat(recibidos[0].getAllRecipients()[0].toString()).isEqualTo("paciente.ficticio@example.com");
        assertThat(recibidos[0].getSubject()).contains("recordatorio");
        assertThat(recibidos[0].getContent().toString()).contains("Consulta/Diagnóstico");
    }

    @Test
    void confirmacionSeEntregaConLosDosEnlacesUnicos() throws Exception {
        when(tokenRepository.save(any(NotificacionToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onTurnoCreado(new TurnoCreadoEvent(turnoFicticio()));

        MimeMessage[] recibidos = greenMail.getReceivedMessages();
        assertThat(recibidos).hasSize(1);
        String cuerpo = recibidos[0].getContent().toString();
        assertThat(cuerpo).contains("http://localhost:8080/api/notifications/confirm?token=");
        assertThat(cuerpo).contains("http://localhost:8080/api/notifications/cancel?token=");
        assertThat(recibidos[0].getSubject()).contains("confirmación");
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
}
