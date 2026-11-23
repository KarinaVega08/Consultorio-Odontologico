package com.consultorio.notification.infrastructure.email;

/**
 * Fallo de entrega de correo. Se captura en el servicio para no bloquear el flujo de turnos.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message) {
        super(message);
    }

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
