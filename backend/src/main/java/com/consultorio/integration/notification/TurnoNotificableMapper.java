package com.consultorio.integration.notification;

import com.consultorio.domain.model.Turno;
import com.consultorio.notification.domain.model.TurnoNotificable;
import org.springframework.stereotype.Component;

/**
 * Convierte la entidad {@code Turno} (C-05) al record público {@code TurnoNotificable}
 * (C-06). Sin estado: lo reutilizan el publisher y el adaptador de recordatorios.
 */
@Component
public class TurnoNotificableMapper {

    public TurnoNotificable toNotificable(Turno turno) {
        return new TurnoNotificable(
            turno.getId(),
            turno.getPaciente().getId(),
            turno.getPaciente().getDni(),
            turno.getPaciente().getEmailRecuperacion(),
            turno.getProfesional().getNombreCompleto(),
            turno.getFechaHoraInicio(),
            turno.getPrestacion().getNombre()
        );
    }
}
