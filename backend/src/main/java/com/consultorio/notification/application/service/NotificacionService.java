package com.consultorio.notification.application.service;

import com.consultorio.domain.exception.BusinessException;
import com.consultorio.notification.application.event.AsistenciaConfirmadaEvent;
import com.consultorio.notification.application.event.TurnoCreadoEvent;
import com.consultorio.notification.application.event.TurnoCanceladoEvent;
import com.consultorio.notification.application.event.TurnoCanceladoPorPacienteEvent;
import com.consultorio.notification.application.event.TurnoReprogramadoEvent;
import com.consultorio.notification.application.port.EmailSenderPort;
import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import com.consultorio.notification.domain.model.ConfiguracionNotificacion;
import com.consultorio.notification.domain.model.NotificacionToken;
import com.consultorio.notification.domain.model.TipoNotificacionToken;
import com.consultorio.notification.domain.model.TurnoNotificable;
import com.consultorio.notification.domain.repository.ConfiguracionNotificacionRepository;
import com.consultorio.notification.domain.repository.NotificacionTokenRepository;
import com.consultorio.notification.infrastructure.email.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Notificaciones por email (RN-NT-01, RN-NT-02).
 * Los fallos de SMTP se loguean y nunca se propagan al flujo de creación de turnos.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionTokenRepository tokenRepository;
    private final ConfiguracionNotificacionRepository configuracionRepository;
    private final EmailSenderPort emailSender;
    private final TurnoParaRecordarPort turnoParaRecordarPort;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${notification.horas-antelacion:24}")
    private int horasPorDefecto;

    @Value("${notification.validez-token-horas:48}")
    private long validezHoras;

    @Value("${notification.base-url:http://localhost:8080}")
    private String baseUrl;

    // ---------------------------------------------------------- Eventos de C-05

    @EventListener
    public void onTurnoCreado(TurnoCreadoEvent event) {
        TurnoNotificable turno = event.turno();
        String confirmToken = crearToken(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, turno);
        String cancelToken = crearToken(TipoNotificacionToken.CANCELAR_TURNO, turno);

        String cuerpo = "Confirmación de turno para " + turno.prestacionNombre()
            + " con " + turno.profesionalNombre() + ".\n"
            + "Confirmar asistencia: " + url(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, confirmToken) + "\n"
            + "Cancelar turno: " + url(TipoNotificacionToken.CANCELAR_TURNO, cancelToken) + "\n"
            + "DNI: " + turno.dniPaciente();

        enviarConFallback(turno.emailPaciente(), "Detalle de su confirmación de turno", cuerpo);
    }

    @EventListener
    public void onTurnoReprogramado(TurnoReprogramadoEvent event) {
        TurnoNotificable turno = event.turno();
        String confirmToken = crearToken(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, turno);
        String cancelToken = crearToken(TipoNotificacionToken.CANCELAR_TURNO, turno);

        String cuerpo = "Su turno fue reprogramado. Nuevo horario: " + turno.fechaHoraInicio() + "\n"
            + "Confirmar asistencia: " + url(TipoNotificacionToken.CONFIRMAR_ASISTENCIA, confirmToken) + "\n"
            + "Cancelar turno: " + url(TipoNotificacionToken.CANCELAR_TURNO, cancelToken);

        enviarConFallback(turno.emailPaciente(), "Reprogramación de su turno", cuerpo);
    }

    @EventListener
    public void onTurnoCancelado(TurnoCanceladoEvent event) {
        TurnoNotificable turno = event.turno();
        enviarConFallback(turno.emailPaciente(), "Cancelación de su turno",
            "Su turno de " + turno.prestacionNombre() + " fue cancelado.");
    }

    // ------------------------------------------------------------- Recordatorios

    /**
     * Recordatorios automáticos. Idempotente: un turno con token RECORDATORIO activo
     * no recibe un segundo envío.
     */
    @Scheduled(cron = "${notification.recordatorio.cron:0 0 8 * * *}")
    public void enviarRecordatorios() {
        int horas = obtenerHorasAntelacion();
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = desde.plusHours(horas);

        for (TurnoNotificable turno : turnoParaRecordarPort.findTurnosEnVentana(desde, hasta)) {
            if (tokenRepository.existsByTurnoIdAndTipoAndUsadoFalse(
                turno.turnoId(), TipoNotificacionToken.RECORDATORIO)) {
                log.debug("Recordatorio ya enviado para el turno {}", turno.turnoId());
                continue;
            }
            crearToken(TipoNotificacionToken.RECORDATORIO, turno);
            enviarConFallback(turno.emailPaciente(), "Su recordatorio de turno",
                "Recordatorio: su turno de " + turno.prestacionNombre()
                    + " con " + turno.profesionalNombre() + " es el " + turno.fechaHoraInicio()
                    + ". DNI: " + turno.dniPaciente());
        }
    }

    // --------------------------------------------------------- Enlaces públicos

    public String confirmarAsistencia(String valorToken) {
        NotificacionToken token = validarToken(valorToken, TipoNotificacionToken.CONFIRMAR_ASISTENCIA);
        consumirToken(token);
        eventPublisher.publishEvent(new AsistenciaConfirmadaEvent(token.getTurnoId(), LocalDateTime.now()));
        log.info("Asistencia confirmada para el turno {}", token.getTurnoId());
        return "Asistencia confirmada";
    }

    public String cancelarTurnoConToken(String valorToken) {
        NotificacionToken token = validarToken(valorToken, TipoNotificacionToken.CANCELAR_TURNO);
        consumirToken(token);
        eventPublisher.publishEvent(new TurnoCanceladoPorPacienteEvent(token.getTurnoId(), LocalDateTime.now()));
        log.info("Cancelación por enlace único para el turno {}", token.getTurnoId());
        return "Turno cancelado";
    }

    private NotificacionToken validarToken(String valorToken, TipoNotificacionToken tipo) {
        NotificacionToken token = tokenRepository.findByToken(valorToken)
            .orElseThrow(() -> new BusinessException("El enlace no es válido"));
        if (token.getTipo() != tipo) {
            throw new BusinessException("El enlace no es válido para esta operación");
        }
        if (Boolean.TRUE.equals(token.getUsado())) {
            throw new BusinessException("El enlace ya fue utilizado");
        }
        if (token.getFechaExpiracion() == null || token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El enlace vencido");
        }
        return token;
    }

    private void consumirToken(NotificacionToken token) {
        token.setUsado(true);
        token.setFechaUso(LocalDateTime.now());
        tokenRepository.save(token);
    }

    // ---------------------------------------------------------- Configuración

    @Transactional(readOnly = true)
    public int obtenerHorasAntelacion() {
        return configuracionRepository.findFirstByActivoTrue()
            .map(ConfiguracionNotificacion::getHorasAntelacion)
            .orElse(horasPorDefecto);
    }

    public int actualizarHorasAntelacion(int horasAntelacion) {
        if (horasAntelacion <= 0) {
            throw new BusinessException("La antelación debe ser mayor a 0 horas");
        }
        ConfiguracionNotificacion configuracion = configuracionRepository.findFirstByActivoTrue()
            .orElseGet(ConfiguracionNotificacion::new);
        configuracion.setHorasAntelacion(horasAntelacion);
        configuracion.setActivo(true);
        configuracionRepository.save(configuracion);
        log.info("Antelación de recordatorios actualizada a {} horas", horasAntelacion);
        return horasAntelacion;
    }

    // ------------------------------------------------------------------ Helper

    String crearToken(TipoNotificacionToken tipo, TurnoNotificable turno) {
        // Invalidar tokens previos no usados del mismo tipo y turno para evitar enlaces duplicados
        List<NotificacionToken> previos = tokenRepository.findByTurnoIdAndTipoAndUsadoFalse(turno.turnoId(), tipo);
        previos.forEach(t -> {
            t.setUsado(true);
            t.setFechaUso(LocalDateTime.now());
            tokenRepository.save(t);
        });

        String valor = UUID.randomUUID().toString().replace("-", "");
        NotificacionToken token = new NotificacionToken();
        token.setToken(valor);
        token.setTipo(tipo);
        token.setTurnoId(turno.turnoId());
        token.setEmailDestino(turno.emailPaciente());
        token.setFechaExpiracion(LocalDateTime.now().plusHours(validezHoras));
        token.setUsado(false);
        token.setActivo(true);
        tokenRepository.save(token);
        return valor;
    }

    private String url(TipoNotificacionToken tipo, String token) {
        String path = switch (tipo) {
            case CONFIRMAR_ASISTENCIA -> "confirm";
            case CANCELAR_TURNO -> "cancel";
            case RECORDATORIO -> "confirm";
        };
        return baseUrl + "/api/notifications/" + path + "?token=" + token;
    }

    private void enviarConFallback(String destinatario, String asunto, String contenido) {
        try {
            emailSender.enviar(destinatario, asunto, contenido);
        } catch (EmailDeliveryException e) {
            // Fallo de SMTP: se registra pero NO rompe la creación/reprogramación del turno.
            log.error("No se pudo enviar correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
