package com.consultorio.api.controller;

import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.api.dto.CatalogResponse;
import com.consultorio.api.dto.CatalogResponse.CatalogItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Datos de solo lectura que el paciente necesita para armar una reserva.
 *
 * <p>No se reutilizan los endpoints {@code /api/admin/**} porque exigen rol
 * ADMINISTRADOR y un paciente autenticado no puede consultarlos.</p>
 */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final ProfesionalRepository profesionalRepository;
    private final PrestacionRepository prestacionRepository;
    private final SillonBoxRepository sillonBoxRepository;

    @GetMapping
    public CatalogResponse get() {
        return new CatalogResponse(
                profesionalRepository.findByActivoTrue().stream()
                        .map(p -> new CatalogItem(p.getId(), p.getNombreCompleto(), null))
                        .toList(),
                prestacionRepository.findByActivoTrue().stream()
                        .map(p -> new CatalogItem(p.getId(), p.getNombre(), p.getDuracionMinutos()))
                        .toList(),
                sillonBoxRepository.findByActivoTrue().stream()
                        .map(s -> new CatalogItem(s.getId(), s.getNombre(), null))
                        .toList()
        );
    }
}
