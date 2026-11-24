package com.consultorio.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Catálogo público necesario para cargar los combos del formulario de reserva.
 * Expone solo id, nombre y duración: nunca entidades JPA.
 */
public record CatalogResponse(
        List<CatalogItem> profesionales,
        List<CatalogItem> prestaciones,
        List<CatalogItem> sillonesBoxes
) {

    public record CatalogItem(UUID id, String nombre, Integer duracionMinutos) {
    }
}
