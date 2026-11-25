# Explore — C-06 integracion-turnos

## Estado actual relevado (`2026-11-23`)

- **`main` en verde**: `./gradlew test` → `BUILD SUCCESSFUL` (70 tests, H2 en memoria). Último commit: `fd7ec18`.
- **`C-06 notificaciones` ya está mergeado**: `com.consultorio.notification` contiene los eventos `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent`, `TurnoCanceladoPorPacienteEvent` y `AsistenciaConfirmadaEvent`; el record `TurnoNotificable`; el puerto `TurnoParaRecordarPort`; `NotificacionService` con `@EventListener`, tokens de un solo uso, scheduler (`@Scheduled`) y enlaces públicos `/api/notifications/confirm|cancel`; `NotificationIntegrationConfig` aporta un bean **no-op** con `@ConditionalOnMissingBean(TurnoParaRecordarPort.class)`.
- **`C-05 gestion-turnos` está en su propia rama** (`feat/c05-gestion-turnos`, commit `f2e9fd1`): proposal/spec/design/tasks sin integración C-06. Aportará `Turno`, `TurnoRepository` y `TurnoService` cuando se mergee.
- **`C-02`** (Matías Oliva) sigue pendiente de merge en `main`; aporta `Paciente.emailRecuperacion`, necesario para mapear el correo.
- **KB**: RN-NT-01 (recordatorio al Gmail vinculado), RN-NT-02 (opción de confirmar asistencia), US-013 (recordatorio automático), CA-6 de la spec de C-06 (el enlace de cancelación publica `TurnoCanceladoPorPacienteEvent` "para que C-05 cambie el estado").

## Brechas detectadas (lo que este change debe cerrar)

1. **Nadie publica** `TurnoCreadoEvent`, `TurnoReprogramadoEvent` ni `TurnoCanceladoEvent` → no hay correos de confirmación/reprogramación/cancelación en la práctica.
2. **`TurnoParaRecordarPort` es no-op** → `enviarRecordatorios()` nunca recibe turnos (CA-2 y RN-NT-01 sin cumplir).
3. **Nadie escucha** `TurnoCanceladoPorPacienteEvent` ni `AsistenciaConfirmadaEvent` → los enlaces únicos consumen el token pero no cambian el estado del turno (CA-6 de C-06 incumplido).
4. **Sin correo vinculado**, publicar un evento haría que el fallback de SMTP intentara enviar a `null` → hay que filtrar esos turnos antes de publicar/incluir en recordatorios.

## Componentes reutilizables (sin modificar)

- Eventos y `TurnoNotificable` de `com.consultorio.notification.application.event` / `.domain.model`.
- `TurnoParaRecordarPort` y `NotificacionService` (ya publica los eventos de enlace y envía correos con fallback).
- `NotificationIntegrationConfig` (su `@ConditionalOnMissingBean` permite reemplazar el no-op sin editarlo).
- `Turno`, `TurnoRepository` y `TurnoService` de C-05, una vez mergeado.
- `GlobalExceptionHandler` y `BusinessException` de C-01 para los caminos de error.

## Conclusión

El propose de esta integración puede ejecutarse **ahora**: es documentación y no depende de que C-05 esté en `main`. El apply sí depende de C-05 + C-02. Cada rama contiene **un solo change**: esta no incluye ningún artefacto de `c-05-gestion-turnos` y no modifica el módulo de C-06.
