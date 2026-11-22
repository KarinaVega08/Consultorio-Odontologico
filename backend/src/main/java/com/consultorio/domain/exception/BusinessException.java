package com.consultorio.domain.exception;

/**
 * Excepción base para reglas de negocio del consultorio.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
