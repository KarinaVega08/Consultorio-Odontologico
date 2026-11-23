package com.consultorio.domain.model;

import java.time.LocalDateTime;

/**
 * Regla estricta de solapamiento, idéntica a la de C-04:
 * dos intervalos se solapan solo cuando {@code inicioA < finB && inicioB < finA}.
 * Por lo tanto, {@code finA == inicioB} permite turnos consecutivos borde a borde.
 */
public final class TurnoSolapamiento {

    private TurnoSolapamiento() {
    }

    public static boolean seSolapan(LocalDateTime inicioA, LocalDateTime finA,
                                    LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }
}
