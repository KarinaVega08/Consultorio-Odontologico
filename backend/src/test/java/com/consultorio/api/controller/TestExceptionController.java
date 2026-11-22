package com.consultorio.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de prueba solo para disparar una RuntimeException y verificar
 * el comportamiento de GlobalExceptionHandler. No es código de producción.
 */
@RestController
public class TestExceptionController {

    @GetMapping("/api/test-exception")
    public void testException() {
        throw new RuntimeException("error de prueba intencional");
    }
}
