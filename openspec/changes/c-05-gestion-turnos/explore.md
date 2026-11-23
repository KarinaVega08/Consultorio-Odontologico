# Explore — C-05 gestion-turnos

## Estado actual relevado (`2026-11-22`)

- **`main` en verde**: `./gradlew test` → `BUILD SUCCESSFUL` (70 tests, 15 clases de test, H2 en memoria, sin servicios externos). Último commit: `fd7ec18`.
- **Cambios ya integrados en `main`**:
  - **`C-01 setup-base`** (archivado): `BaseEntity` (UUID, `activo`, auditoría), enum `EstadoTurno` (`RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`), `BusinessException`, `ResourceNotFoundException`, `GlobalExceptionHandler`, `SecurityConfig` (whitelist pública, resto autenticado, STATELESS), `AdminSecurityConfig` (`@EnableMethodSecurity`).
  - **`C-03 configuracion-consultorio`** (archivado): entidades y repositorios `Profesional`, `SillonBox`, `Prestacion` (con `duracionMinutos`), `HorarioAtencion`, `Bloqueo` + CRUD admin en `AdminConfigController`.
  - **`C-06 notificaciones`** (archivado, **compartido con Kari**): eventos `TurnoCreadoEvent`, `TurnoReprogramadoEvent`, `TurnoCanceladoEvent`, `TurnoCanceladoPorPacienteEvent`, `AsistenciaConfirmadaEvent`; record `TurnoNotificable`; puerto `TurnoParaRecordarPort` con **bean no-op** en `NotificationIntegrationConfig` (`@ConditionalOnMissingBean`); `NotificacionService` con listeners `@EventListener`, tokens de un solo uso, scheduler y enlaces públicos `/api/notifications/confirm|cancel`.
- **Pendiente de merge**: `feat/c02-registro-pacientes` (Mati, completo y testeado): entidad `Paciente` (`dni`, `emailRecuperacion`), `TokenService.AuthenticatedUser` y `BearerTokenAuthenticationFilter` → **bloquea el apply** de este change (FK a `Paciente` y RBAC real).
- **Solo propose**: `feat/c04-disponibilidad-turnos` (Mai): spec con la regla estricta `inicioA < finB && inicioB < finA`, exclusión de `CANCELADO` y `RR-04` ("la lógica de cálculo debe ser reutilizable por `C-05`"); su `tasks.md` A3 pide confirmar con Caste el modelo de `Turno`.
- **Especificaciones relacionadas**: `openspec/changes/c-04-disponibilidad-turnos/spec.md`, `openspec/changes/c-06-notificaciones/spec.md` (CA-6: la cancelación desde el enlace único publica `TurnoCanceladoPorPacienteEvent` "para que C-05 cambie el estado").

## Componentes reutilizables ya disponibles

- `BaseEntity`, `EstadoTurno`, excepciones de negocio y `GlobalExceptionHandler` (`C-01`).
- Repositorios y entidades de agenda de `C-03`: `HorarioAtencionRepository`, `BloqueoRepository`, `PrestacionRepository`, `ProfesionalRepository`, `SillonBoxRepository`.
- Eventos, record `TurnoNotificable`, puerto `TurnoParaRecordarPort` y `ApplicationEventPublisher` de `C-06`.
- Cadenas de seguridad: `SecurityConfig` (autenticación requerida fuera de la whitelist) y `AdminSecurityConfig` (`@PreAuthorize` ya habilitado).
- De `C-02` (en rama): `PacienteRepository`, `AuthenticatedUser` y el filtro Bearer, una vez mergeado.

## Brechas detectadas para C-05

1. **No existe `Turno`**: ni entidad, ni repositorio, ni DTOs, ni `TurnoService`, ni `AppointmentController` — es el hueco central de este change.
2. **Nadie escucha `TurnoCanceladoPorPacienteEvent` ni `AsistenciaConfirmadaEvent`** (verificado en el código: solo se publican desde `NotificacionService`) → la cancelación/confirmación desde los enlaces únicos hoy no cambia ningún estado.
3. **`TurnoParaRecordarPort` sigue en no-op** → los recordatorios automáticos de `C-06` nunca encuentran turnos.
4. **IN-04 sin resolver**: la KB propone `EXCLUDE USING gist` de PostgreSQL, pero la regla dura exige tests en verde sobre H2, que no soporta constraints de exclusión → hace falta una estrategia compatible con ambos (resuelta en `design.md` D2: lock pesimista + regla estricta en la app, constraint PG idempotente solo en runtime).
5. **`C-02` sin merge**: sin `Paciente` ni autenticación real no se puede codificar el apply (el propose sí puede avanzar).
6. **Riesgo de integración de paquetes**: `C-04` diseñó contra `domain.model.Turno` / `domain.repository.TurnoRepository` mientras `C-03`/`C-06` usan paquetes feature → decisión registrada en `design.md` D1 para no romper a nadie.

## Conclusión

El propose de C-05 puede ejecutarse **ahora** sin esperar a nadie (es documentación de planificación); el apply queda a la espera del merge de `C-02` y, opcionalmente, de que `C-04` mergee para reusar su calculadora de disponibilidad.
