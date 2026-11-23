package com.consultorio.notification.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailSender sender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sender, "from", "consultorio@example.com");
    }

    @Test
    void enviarConstruyeMimeMessageConDestinatarioAsuntoYCuerpo() throws Exception {
        sender.enviar("paciente.ficticio@example.com", "Confirmación de turno", "Hola, su turno fue reservado");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage mensaje = captor.getValue();
        assertThat(mensaje.getAllRecipients()[0].toString()).isEqualTo("paciente.ficticio@example.com");
        assertThat(mensaje.getSubject()).isEqualTo("Confirmación de turno");
        assertThat(mensaje.getFrom()[0].toString()).isEqualTo("consultorio@example.com");
    }

    @Test
    void errorDeEnvioSeConvierteEnEmailDeliveryException() {
        doThrow(new MailSendException("SMTP caído"))
            .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> sender.enviar("paciente.ficticio@example.com", "Asunto", "Cuerpo"))
            .isInstanceOf(EmailDeliveryException.class)
            .hasMessageContaining("SMTP caído");
    }
}
