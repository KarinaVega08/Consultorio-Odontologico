package com.consultorio.domain.availability;

import java.time.LocalDateTime;

/**
 * Intervalo no disponible por profesional o por sillón/box.
 */
public record Block(LocalDateTime inicio, LocalDateTime fin, TipoBloqueo tipo) {
}
