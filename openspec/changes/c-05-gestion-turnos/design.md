# Design — C-05 gestion-turnos

## Context

`main` está en verde (70 tests, H2 en memoria) con la estructura por capas de `C-01`, las entidades de agenda de `C-03` (`Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo`) y el módulo completo de `C-06` (eventos, `TurnoNotificable`, `TurnoParaRecordarPort` en no-op). `C-02` (entidad `Paciente`, usuario autenticado y filtro Bearer) todavía no está mergeado y `C-04` solo tiene propuesta. La motivación está en `proposal.md` (Why) y el comportamiento exigido en `specs/gestion-turnos/spec.md`. Este design resuelve sobre todo **IN-04** (estrategia de bloqueo en BD) y la integración con `C-06`.

## Goals / Non-Goals

**Goals:**

- Crear, reprogramar y cancelar turnos con anti-solapamiento **transaccional** por profesional y sillón/box, correcto ante reservas simultáneas.
- Compatible con **H2 (tests) y PostgreSQL (runtime)** sin romper la regla dura de tests autocontenidos (RN-EX-02).
- Integrarse con `C-06` **sin modificar su código** (eventos, puerto y listeners).
- Cubrir con tests los escenarios del spec (feliz, error de negocio, borde, seguridad e integración).

**Non-Goals:**

- Calcular disponibilidad de franjas (`C-04`), autenticación (`C-02`) ni enviar emails (`C-06`).
- Frontend de reserva, multi-sucursal, odontograma y todo lo excluido del MVP.
- Cambios en la whitelist de `SecurityConfig` o en el modelo de datos de `C-03`.

## Decisions

### D1 — Ubicación de componentes (capas raíz, como C-01/C-02)

- `com.consultorio.domain.model.Turno` (extiende `BaseEntity`) y `com.consultorio.domain.repository.TurnoRepository`.
- `com.consultorio.application.dto.*` (requests con `@Valid` y responses), `com.consultorio.application.service.TurnoService`.
- `com.consultorio.api.controller.AppointmentController` (`/api/appointments`).

**Por qué**: es la misma estructura que `C-01`/`C-02` usan y la que el `design.md` de `C-04` ya espera (`domain.model.Turno`, `domain.repository.TurnoRepository`), de modo que su `AvailabilityService` compila contra lo que este change entregue sin fricción. **Alternativa descartada**: paquete feature `com.consultorio.turno.*` (patrón de `C-03`/`C-06`), que obligaría a imports cruzados contra lo ya diseñado en `C-04`; queda como deuda si el equipo unifica estilos después del MVP.

### D2 — Anti-solapamiento y concurrencia (resuelve IN-04)

Guard en dos capas, dentro de una misma transacción (`@Transactional` en el método de servicio):

1. **Lock pesimista sobre los recursos contendientes**: antes de validar, bloquear la fila del profesional y la del sillón/box (`SELECT ... FOR UPDATE` vía `@Lock(PESSIMISTIC_WRITE)`), siempre en el mismo orden (profesional primero, desempatado por id) para evitar deadlocks. Esto serializa las reservas simultáneas sobre el mismo profesional o sillón y **funciona igual en H2 y en PostgreSQL**.
2. **Validación de solapes con la regla estricta** `inicioA < finB && inicioB < finA` sobre turnos que no estén `CANCELADO` (idéntica a la spec de `C-04`).
3. **Constraint de exclusión PostgreSQL** (`EXCLUDE USING gist` sobre profesional+rango y sillón+rango) como defensa en profundidad: script idempotente `schema-pg.sql` ejecutado **solo en runtime con PostgreSQL** (`spring.sql.init.mode` configurable por variable de entorno), nunca en los tests H2, que no lo soportan.

**Alternativas descartadas**: solo el constraint PG (rompe `./gradlew test` en H2, viola RN-EX-02); solo la validación sin lock (race condition: dos reservas simultáneas pasan ambas validaciones); versionado optimista con `@Version` (requiere reintentos y no garantiza por sí solo la ausencia de doble reserva).

### D3 — `fecha_hora_fin` y validación RN-TU-03

- `fecha_hora_fin` se calcula **siempre en el servidor** = `fecha_hora_inicio` + `duracionMinutos` de la `Prestacion` elegida; el cliente nunca la envía.
- Se valida que el turno esté **contenido completo** en el horario laboral vigente (`HorarioAtencion` del profesional para el día de la fecha) y que **no choque con bloqueos** (`Bloqueo` del profesional o del sillón/box), usando los repositorios ya existentes de `C-03`.
- **Coordinación con `C-04`**: mismo criterio de solapamiento y misma exclusión de `CANCELADO`. Si el `AvailabilityCalculator` de Mai mergea antes que este apply, el servicio puede delegarle el cálculo (confirmarlo con Mai — su tarea A3); hasta entonces `C-05` valida con las entidades de `C-03` para no depender de una rama sin merge.

### D4 — Integración con C-06 sin tocar su código

- **Mapper a `TurnoNotificable`**: `turnoId`, `pacienteId`, `dniPaciente`, `emailPaciente` (`Paciente.emailRecuperacion`), `profesionalNombre`, `fechaHoraInicio`, `prestacionNombre`.
- **Publicación** de `TurnoCreadoEvent`, `TurnoReprogramadoEvent` y `TurnoCanceladoEvent` con `ApplicationEventPublisher` **al final del método de servicio** (después del `save()`), para minimizar envíos de correo ante un rollback posterior.
- **Sin Gmail vinculado → sin evento**: si `emailRecuperacion` es `null`, no se publica el evento (evita el log de error del fallback de `C-06`).
- **`TurnoParaRecordarPort`**: implementación `@Component` que devuelve turnos `RESERVADO`/`CONFIRMADO` de la ventana solicitada con paciente con email; el bean no-op de `NotificationIntegrationConfig` desaparece solo por su `@ConditionalOnMissingBean`.
- **Listeners `@EventListener`** (en `TurnoService`): `TurnoCanceladoPorPacienteEvent` → estado `CANCELADO`; `AsistenciaConfirmadaEvent` → estado `CONFIRMADO`. Si el turno no existe o ya está cancelado, se registra el hecho (SLF4J) y **no se propaga excepción** (los enlaces son públicos y no deben romperse).
- No se modifica `SecurityConfig`: los enlaces `/api/notifications/**` ya tienen su propia cadena en `NotificationSecurityConfig`.

### D5 — RBAC de los endpoints de turnos

- Todos los endpoints requieren autenticación (whitelist de `C-01` no se toca).
- **Paciente**: solo opera sobre sus propios turnos (`pacienteId == usuario autenticado`); en `POST` solo puede reservar para sí mismo.
- **Recepcionista / Odontólogo / Administrador**: operan sobre cualquier turno (búsqueda de paciente por DNI, Flujo 3). Autorización por método con `@PreAuthorize`, ya habilitada por `AdminSecurityConfig` (`@EnableMethodSecurity`).
- Identificación del actor vía `@AuthenticationPrincipal` del usuario de `C-02` → **requiere merge de `C-02` antes del apply**.

### D6 — Manejo de errores

`BusinessException` → `400` (solapamiento, fuera de horario, bloqueo, turno ya cancelado), `ResourceNotFoundException` → `404` (turno/profesional/prestación inexistente), `401`/`403` de seguridad, todo manejado por el `GlobalExceptionHandler` existente. DTOs con Bean Validation (`@NotNull`, `@FutureOrPresent` para el inicio).

## Risks / Trade-offs

- **[El apply depende de `C-02` sin merge]** → el propose no depende de nadie; mantener esta spec y hacer rebase en cuanto se mergeee antes de codificar. Avisar a Thiago.
- **[Lock pesimista serializa reservas del mismo profesional]** → volumen de un consultorio: irrelevante; si algún día escala, migra al constraint PG + reintentos.
- **[Doble guarda (app + constraint PG)]** → una carrera rara puede violar el constraint tras pasar la app; se captura la violación y se responde `400` con el mismo mensaje, manteniendo el comportamiento observable.
- **[Eventos publicados antes del commit real]** → un rollback posterior podría dejar un correo enviado sin turno; riesgo aceptado y registrado (mismo comportamiento que `C-06` hoy), mitigado publicando al final del método.
- **[Estilos de paquetes mezclados en el repo]** → decisión registrada en D1; unificar es trabajo post-MVP, no de este change.

## Migration Plan

No aplica: JPA crea la tabla `turnos` con `ddl-auto: update` (sin borrados ni renombres) y `schema-pg.sql` es idempotente. Rollback = revert del commit; ningún dato existente se modifica.

## Open Questions

- **Granularidad de la grilla (IN-05, 15 o 30 min)**: decisión de `C-04`; no afecta a `C-05`, que recibe fecha/hora exacta del cliente.
- **UI de reserva para la demo del 25/11**: fuera de alcance de este change; si el equipo la quiere, se plantea como change nuevo (regla: nada de funcionalidad fuera de los changes).
