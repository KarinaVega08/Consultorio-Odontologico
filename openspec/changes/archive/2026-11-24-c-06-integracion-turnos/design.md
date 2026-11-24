# Design — C-06 integracion-turnos

## Context

`main` ya contiene el módulo completo de `C-06` (`com.consultorio.notification`): eventos de turno, record `TurnoNotificable`, puerto `TurnoParaRecordarPort` con bean no-op, `NotificacionService` con `@EventListener`, tokens de un solo uso, scheduler y enlaces públicos. Sin embargo, **nada lo conecta con la agenda**: no se publican los tres eventos de entrada, el puerto devuelve lista vacía y los dos eventos de los enlaces no tienen consumidor. `C-05 gestion-turnos` (rama `feat/c05-gestion-turnos`) aportará la entidad `Turno`, el repositorio y el servicio; por eso el apply de este change va **después** del merge de C-05. Este design cubre solo el pegamento (Why en `proposal.md`, comportamiento en `specs/notificaciones-turnos/spec.md`).

## Goals / Non-Goals

**Goals:**

- Que el correo de confirmación, reprogramación, cancelación y los recordatorios **sucedan de verdad** al usar la agenda.
- Que cancelar/confirmar desde los enlaces únicos se refleje en el estado del turno.
- Integrarse **sin modificar** `com.consultorio.notification` ni duplicar lógica de C-05.
- Tests sobre H2, con SMTP simulado y datos ficticios (Ley 25.326).

**Non-Goals:**

- Rehacer el envío de correos, tokens, scheduler o configuración de antelación (ya están en C-06).
- Crear la entidad `Turno`, los endpoints `/api/appointments` o las reglas de solapamiento (C-05).
- Cambiar la whitelist de seguridad o el frontend.

## Decisions

### D1 — Paquete de integración separado

- Todo el código nuevo vive en `com.consultorio.integration.notification`: `TurnoNotificableMapper`, `TurnosNotificacionPublisher`, `TurnoRecordatorioAdapter` y `TurnoEstadoPorNotificacionListener`.
- El publisher se invoca desde `TurnoService` (C-05) con **una llamada por operación** tras persistir; el resto de la lógica de turnos no se toca.

**Por qué**: mantiene un único lugar donde C-05 y C-06 se acoplan, deja el módulo de C-06 intacto (contrato público) y evita mezclar responsabilidades en la rama de C-05. **Alternativa descartada**: meter mapper/listeners dentro de `com.consultorio.notification` (modificaría C-06 y rompería la separación de ramas acordada).

### D2 — Publicación de eventos

- `TurnosNotificacionPublisher` usa `ApplicationEventPublisher` con los eventos existentes: `TurnoCreadoEvent(TurnoNotificable)`, `TurnoReprogramadoEvent(TurnoNotificable, fechaHoraAnterior)` y `TurnoCanceladoEvent(TurnoNotificable)`.
- Se llama **al final del método de servicio**, después de `save()` y dentro de `@Transactional`, para no publicar si falla la validación.
- **Sin correo → sin evento**: si `Paciente.emailRecuperacion` es `null`, el publisher no publica (evita el log de error del fallback SMTP de C-06).

**Alternativa descartada**: publicar desde el controller (perdería los eventos cuando el servicio se llama desde otros flujos) o desde un `@TransactionalEventListener` en C-06 (obligaría a modificar C-06).

### D3 — Mapper `Turno` → `TurnoNotificable`

`turnoId` = `Turno.id`; `pacienteId` y `dniPaciente` = del `Paciente` (C-02); `emailPaciente` = `emailRecuperacion`; `profesionalNombre` = `Profesional.nombre`; `fechaHoraInicio` = `Turno.fechaHoraInicio`; `prestacionNombre` = `Prestacion.nombre`. El mapper es un componente sin estado, reutilizado por el publisher y por el adaptador de recordatorios.

### D4 — Implementación de `TurnoParaRecordarPort`

- `TurnoRecordatorioAdapter implements TurnoParaRecordarPort` con `findTurnosEnVentana(desde, hasta)`.
- Consulta `TurnoRepository` por `estado IN (RESERVADO, CONFIRMADO)` y `fechaHoraInicio BETWEEN :desde AND :hasta`, con `Paciente.emailRecuperacion` no nulo (proyección o join, nunca entidades JPA en la API).
- Mapea cada resultado con el mapper de D3. Al existir este `@Component`, el bean no-op de `NotificationIntegrationConfig` desaparece por su `@ConditionalOnMissingBean(TurnoParaRecordarPort.class)` — **sin tocar esa clase**.

### D5 — Listeners de enlaces únicos

- `TurnoEstadoPorNotificacionListener` con `@EventListener` para `TurnoCanceladoPorPacienteEvent` y `AsistenciaConfirmadaEvent`, delegando en métodos de `TurnoService` que reutilizan la lógica de C-05 (cancelación lógica / confirmación).
- Son **idempotentes**: si el turno no existe o ya está `CANCELADO`/`CONFIRMADO`, se registra con SLF4J y no se lanza excepción (los enlaces son públicos y no deben romperse).
- `publishEvent` de Spring es síncrono: el estado queda actualizado antes de que el controlador devuelva la respuesta del enlace, lo que facilita el test de integración.

### D6 — Seguridad y datos

- No se modifica `SecurityConfig` ni `NotificationSecurityConfig`: los enlaces siguen protegidos por token de un solo uso y `/api/appointments` sigue autenticado (C-05).
- Todos los datos de tests son ficticios (DNI 99.999.999, correos de ejemplo); ningún secret en el repo (SMTP por variables de entorno).

## Risks / Trade-offs

- **[El apply depende de C-05 mergeado]** → el propose avanza ahora; el apply se hace solo cuando `TurnoService` y `TurnoRepository` existan en `main`. No bloquea esta rama de documentación.
- **[Evento publicado antes del commit]** → un rollback posterior podría dejar un correo enviado sin turno persistido. Mitigación: publicar al final del servicio y cubrir con tests el camino de error; riesgo aceptado en el MVP.
- **[Doble cancelación (API + enlace)]** → ambos caminos convergen en `CANCELADO`; la idempotencia de D5 evita excepciones y mantiene el comportamiento observable.
- **[Sin correo vinculado → sin correo de confirmación]** → decisión deliberada para no disparar el fallback de SMTP con destinatario nulo; queda registrada en el spec.
- **[Acoplamiento entre ramas]** → esta rama no incluye código de C-05; los tests.apply se escriben después del merge. Es el coste de mantener un change por rama como pidió el equipo.

## Migration Plan

No aplica: no hay cambios de esquema ni de datos. Rollback = revert del commit de integración; C-06 vuelve a su bean no-op y C-05 sigue funcionando sin notificaciones.

## Open Questions

- **Orden de merge**: Thiago decide simergea primero C-05 y luego esta integración; el propose no depende del orden, el apply sí.
- **¿Confirmar asistencia sobre un turno cancelado?** Se decide mantenerlo como `CANCELADO` (no reactivar); si el negocio quiere reactivación, será un change nuevo.
