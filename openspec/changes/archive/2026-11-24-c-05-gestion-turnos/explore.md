# Explore — C-05 gestion-turnos

## Estado actual relevado (`2026-11-22`)

- **`main` en verde**: `./gradlew test` → `BUILD SUCCESSFUL` (70 tests, 15 clases de test, H2 en memoria, sin servicios externos). Último commit: `fd7ec18`.
- **Cambios ya integrados en `main`**:
  - **`C-01 setup-base`** (archivado): `BaseEntity` (UUID, `activo`, auditoría), enum `EstadoTurno` (`RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`), `BusinessException`, `ResourceNotFoundException`, `GlobalExceptionHandler`, `SecurityConfig` (whitelist pública, resto autenticado, STATELESS), `AdminSecurityConfig` (`@EnableMethodSecurity`).
  - **`C-03 configuracion-consultorio`** (archivado): entidades y repositorios `Profesional`, `SillonBox`, `Prestacion` (con `duracionMinutos`), `HorarioAtencion`, `Bloqueo` + CRUD admin en `AdminConfigController`.
  - **`C-06 notificaciones`** (archivado): eventos `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent`, `TurnoCanceladoPorPacienteEvent`, `AsistenciaConfirmadaEvent`; record `TurnoNotificable`; puerto `TurnoParaRecordarPort` con **bean no-op** en `NotificationIntegrationConfig` (`@ConditionalOnMissingBean`); `NotificacionService` con listeners `@EventListener`, tokens de un solo uso, scheduler y enlaces públicos `/api/notifications/confirm|cancel`. Su integración con la agenda **no** está en esta rama: se planifica en `feat/c06-integracion-turnos`.
- **Pendiente de merge**: `feat/c02-registro-pacientes` (Matías Oliva, completo y testeado): entidad `Paciente` (`dni`, `emailRecuperacion`), `TokenService.AuthenticatedUser` y `BearerTokenAuthenticationFilter` → **bloquea el apply** de este change (FK a `Paciente` y RBAC real).
- **Solo propose**: `feat/c04-disponibilidad-turnos` (Mailén Claros): spec con la regla estricta `inicioA < finB && inicioB < finA`, exclusión de `CANCELADO` y `RR-04` ("la lógica de cálculo debe ser reutilizable por `C-05`"); su `tasks.md` A3 pide confirmar con Franco Castellarín el modelo de `Turno`.
- **Especificación relacionada**: `openspec/changes/c-04-disponibilidad-turnos/spec.md`.

## Componentes reutilizables ya disponibles

- `BaseEntity`, `EstadoTurno`, excepciones de negocio y `GlobalExceptionHandler` (`C-01`).
- Repositorios y entidades de agenda de `C-03`: `HorarioAtencionRepository`, `BloqueoRepository`, `PrestacionRepository`, `ProfesionalRepository`, `SillonBoxRepository`.
- Cadenas de seguridad: `SecurityConfig` (autenticación requerida fuera de la whitelist) y `AdminSecurityConfig` (`@PreAuthorize` ya habilitado).
- De `C-02` (en rama): `PacienteRepository`, `AuthenticatedUser` y el filtro Bearer, una vez mergeado.

## Brechas detectadas para C-05

1. **No existe `Turno`**: ni entidad, ni repositorio, ni DTOs, ni `TurnoService`, ni `AppointmentController` — es el hueco central de este change.
2. **IN-04 sin resolver**: la KB propone `EXCLUDE USING gist` de PostgreSQL, pero la regla dura exige tests en verde sobre H2, que no soporta constraints de exclusión → hace falta una estrategia compatible con ambos (resuelta en `design.md` D2: lock pesimista + regla estricta en la app, constraint PG idempotente solo en runtime).
3. **`C-02` sin merge**: sin `Paciente` ni autenticación real no se puede codificar el apply (el propose sí puede avanzar).
4. **Riesgo de integración de paquetes**: `C-04` diseñó contra `domain.model.Turno` / `domain.repository.TurnoRepository` mientras `C-03`/`C-06` usan paquetes feature → decisión registrada en `design.md` D1 para no romper a nadie.

## Fuera de alcance de esta rama (se planifican en `feat/c06-integracion-turnos`)

- Nadie escucha `TurnoCanceladoPorPacienteEvent` ni `AsistenciaConfirmadaEvent` (solo se publican desde `NotificacionService`) → la cancelación/confirmación desde enlaces únicos hoy no cambia ningún estado.
- `TurnoParaRecordarPort` sigue en no-op → los recordatorios automáticos nunca encuentran turnos.
- Los eventos `TurnoCreadoEvent`, `TurnoReprogramadoEvent` y `TurnoCanceladoEvent` todavía no se publican desde la agenda (porque la agenda no existe).

## Conclusión

El propose de C-05 puede ejecutarse **ahora** sin esperar a nadie (es documentación de planificación); el apply queda a la espera del merge de `C-02` y, opcionalmente, de que `C-04` mergee para reusar su calculadora de disponibilidad. Cada rama contiene **un solo change**: esta no incluye ningún artefacto de la integración con `C-06`.
