package com.consultorio.api.controller;

import com.consultorio.application.dto.AvailabilityRequest;
import com.consultorio.application.dto.AvailabilityResponse;
import com.consultorio.application.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta pública de franjas libres (RF-01). La ruta ya está en la whitelist de seguridad.
 */
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public AvailabilityResponse consultar(@Valid @ModelAttribute AvailabilityRequest request) {
        return availabilityService.consultar(request);
    }
}
