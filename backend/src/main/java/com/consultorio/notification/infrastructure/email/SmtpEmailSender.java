package com.consultorio.notification.infrastructure.email;

import com.consultorio.notification.application.port.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Adaptador SMTP: Gmail en producción, Mailpit en desarrollo (según SMTP_HOST/SMTP_PORT).
 * Nunca incluye credenciales en el código: vienen de variables de entorno.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${notification.from:consultorio@example.com}")
    private String from;

    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        try {
            MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido);
            mailSender.send(mensaje);
            log.info("Correo enviado a {}", destinatario);
        } catch (MailException e) {
            throw new EmailDeliveryException("Error al enviar correo: " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new EmailDeliveryException("No se pudo construir el correo: " + e.getMessage(), e);
        }
    }
}
