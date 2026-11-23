# Proposal — C-06 integracion-turnos

## Why

`C-06 notificaciones` ya está mergeado en `main` con eventos, tokens de un solo uso, scheduler y enlaces públicos, pero **está desconectado de la agenda**: nadie publica `TurnoCreadoEvent`, `TurnoReprogramadoEvent` ni `TurnoCanceladoEvent`, `TurnoParaRecordarPort` sigue siendo un bean no-op y los eventos `TurnoCanceladoPorPacienteEvent` / `AsistenciaConfirmadaEvent` que emiten los enlaces únicos no cambian ningún estado. En la práctica no llegan correos de confirmación ni recordatorios, y cancelar/confirmar desde un enlace no impacta el turno (RN-NT-01, RN-NT-02, US-013). Esta es la parte de la integración que completa **Caste** junto con **Kari**, y depende de que `C-05 gestion-turnos` esté mergeado.

## What Changes

- **Mapper** de la entidad `Turno` (de `C-05`) al record `TurnoNotificable` ya existente en `C-06` (`turnoId`, `pacienteId`, `dniPaciente`, `emailPaciente`, `profesionalNombre`, `fechaHoraInicio`, `prestacionNombre`).
- **Publicación** de `TurnoCreadoEvent`, `TurnoReprogramadoEvent` (con `fechaHoraAnterior`) y `TurnoCanceladoEvent` al crear, reprogramar y cancelar turnos por API.
- **Implementación real de `TurnoParaRecordarPort`**: turnos `RESERVADO`/`CONFIRMADO` dentro de la ventana solicitada, con paciente que tenga email; el bean no-op de `NotificationIntegrationConfig` desaparece automáticamente por su `@ConditionalOnMissingBean`.
- **Listeners** de `TurnoCanceladoPorPacienteEvent` → estado `CANCELADO` (sin borrado físico) y `AsistenciaConfirmadaEvent` → estado `CONFIRMADO`, idempotentes y sin propagar excepciones al enlace público.
- **Sin email vinculado → sin publicación** de eventos ni inclusión en recordatorios, para no disparar el fallback de SMTP con destinatario nulo.
- **Cero cambios en el módulo `com.consultorio.notification` de `C-06`**: solo se consume su contrato público (eventos, record y puerto).

## Capabilities

### New Capabilities

- `notificaciones-turnos`: pegamento entre la agenda de turnos y el módulo de notificaciones — publicación de eventos desde la gestión de turnos, consulta real de turnos a recordar y actualización de estado del turno desde los enlaces únicos.

### Modified Capabilities

<!-- Ninguna. La capacidad `notificaciones` de C-06 no cambia: este change solo la conecta con la agenda. -->

## Impact

- **Código nuevo** (paquete de integración, sin tocar `C-06` ni la lógica core de `C-05`): `com.consultorio.integration.notification.*` — mapper, publicador de eventos, adaptador de `TurnoParaRecordarPort` y listeners de cambio de estado. Se añaden además las llamadas al publicador desde `TurnoService` (C-05), una por operación.
- **Dependencias**: requiere **`C-05` mergeado** (`Turno`, `TurnoRepository`, `TurnoService`) y **`C-02`** (`Paciente.emailRecuperacion`); consume eventos y contratos ya presentes en `main` desde `C-06`.
- **Seguridad**: no se modifica `SecurityConfig` ni la `NotificationSecurityConfig`; los enlaces `/api/notifications/**` siguen públicos (token) y los endpoints `/api/appointments` siguen autenticados (C-05).
- **BD**: sin migraciones nuevas; la cancelación por enlace usa el mismo cambio de estado de C-05 (nunca `DELETE`).
- **Tests**: batería sobre H2 con datos ficticios (RN-EX-01 / RN-EX-02), sin SMTP real ni servicios externos.

## Leer antes

- `knowledge-base/05_reglas_de_negocio.md` — §RN-NT-01, §RN-NT-02, §RN-EX-01, §RN-EX-02
- `knowledge-base/06_funcionalidades.md` — §Épica 4 (US-013)
- `openspec/changes/c-06-notificaciones/spec.md` — CA-1, CA-2, CA-6 y CA-7 (contrato que se debe cumplir al conectarlo)
- `backend/src/main/java/com/consultorio/notification/` — eventos, `TurnoNotificable`, `TurnoParaRecordarPort`, `NotificacionService`
