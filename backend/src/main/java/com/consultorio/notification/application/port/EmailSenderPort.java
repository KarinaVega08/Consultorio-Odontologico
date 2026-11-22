package com.consultorio.notification.application.port;

/**
 * Puerto de envío de correo. La implementación real usa SMTP (Gmail/Mailpit).
 */
public interface EmailSenderPort {

    void enviar(String destinatario, String asunto, String contenido);
}
