package com.consultorio.domain.exception;

/**
 * Excepción lanzada cuando un recurso no existe en el sistema.
 * Mapea a HTTP 404 en la API REST.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
