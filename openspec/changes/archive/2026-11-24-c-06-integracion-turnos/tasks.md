# Tasks — C-06 integracion-turnos

> Checklist de trabajo para **Franco Castellarín** (con **Karina Vega** como co-responsable). Rama: `feat/c06-integracion-turnos` (solo este change).
> Referencias: `specs/notificaciones-turnos/spec.md` (qué) y `design.md` (cómo).
> Cero archivos de `c-05-gestion-turnos` en esta rama: el apply requiere que C-05 ya esté mergeado en `main`.

## 1. Coordinación y preparación (antes de codear)

- [x] 1.1 Confirmar con **Karina Vega** que el contrato existente (eventos, `TurnoNotificable`, `TurnoParaRecordarPort`) no se modifica — verificación: mensaje en el chat del equipo con el OK de Karina Vega.
- [x] 1.2 Verificar que **`C-05` esté mergeado** en `main` antes del apply (`Turno`, `TurnoRepository`, `TurnoService`) — verificación: `git log origin/main --oneline` incluye el merge de `feat/c05-gestion-turnos`.
- [x] 1.3 Confirmar en verde la base antes de empezar — verificación: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`.

## 2. Ciclo OPSX — Explore y Propose

- [x] 2.1 Explore: relevar el módulo `notification` de `main` y las brechas de conexión en `explore.md` — verificación: archivo presente.
- [x] 2.2 Propose: `proposal.md` con Why, What Changes y capacidad `notificaciones-turnos` — verificación: `openspec status --change c-06-integracion-turnos` marca `proposal` done.
- [x] 2.3 Spec: `specs/notificaciones-turnos/spec.md` con 7 requirements y 10 escenarios WHEN/THEN (feliz, error, borde, seguridad) — verificación: `openspec validate c-06-integracion-turnos --strict` sin errores.
- [x] 2.4 Design: `design.md` con decisiones D1-D6 (sin tocar C-06) — verificación: archivo presente con todas las secciones.
- [x] 2.5 Tasks: este archivo — verificación: `openspec status --change c-06-integracion-turnos` → todos los artefactos `done`.
- [x] 2.6 Subir la rama a `origin/feat/c06-integracion-turnos` (sin PR: la revisión y el merge los hace Thiago Sinatra) — verificación: `git ls-remote origin refs/heads/feat/c06-integracion-turnos`.

## 3. Integración (apply, después del merge de C-05)

- [x] 3.1 Crear `TurnoNotificableMapper` y `TurnosNotificacionPublisher` en `com.consultorio.integration.notification` (D1/D2/D3) — verificación: test unitario que construye el `TurnoNotificable` completo a partir de un turno ficticio.
- [x] 3.2 Publicar `TurnoCreadoEvent`, `TurnoReprogramadoEvent` (con `fechaHoraAnterior`) y `TurnoCanceladoEvent` desde `TurnoService` al crear/reprogramar/cancelar; sin evento si no hay email — verificación: tests que capturan los eventos publicados y el caso sin correo.
- [x] 3.3 Implementar `TurnoRecordatorioAdapter implements TurnoParaRecordarPort` filtrando estados, ventana y correo, reemplazando el no-op — verificación: test que devuelve solo los turnos válidos y comprueba que el bean no-op ya no está activo.
- [x] 3.4 Implementar `TurnoEstadoPorNotificacionListener` (`@EventListener`) para `TurnoCanceladoPorPacienteEvent` → `CANCELADO` y `AsistenciaConfirmadaEvent` → `CONFIRMADO`, idempotente — verificación: tests de integración con tokens reales y casos borde (ya cancelado / inexistente).

## 4. Verificación integral (verify)

- [x] 4.1 Correr la suite completa en verde: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`, 0 fallos, H2 en memoria y SMTP simulado, sin servicios externos — verificación: salida de Gradle con los escenarios del spec cubiertos.
- [x] 4.2 Confirmar que **no se modificó** `backend/src/main/java/com/consultorio/notification/` ni la configuración de seguridad — verificación: `git diff --name-only origin/main..HEAD` no incluye esas rutas.
- [x] 4.3 Revisar reglas duras: datos ficticios (Ley 25.326) y cero secrets — verificación: sin DNIs/emails reales ni credenciales en el diff.

## 5. Cierre de esta rama

- [x] 5.1 Dejar la rama lista para revisión: commit de planificación pusheado y cero archivos de C-05 — verificación: `git diff --name-only origin/main..HEAD` solo muestra rutas bajo `openspec/changes/c-06-integracion-turnos/`.

## Workflow follow-up

- La revisión, el merge y el archivado del change los hace **Thiago Sinatra** (no se ejecutan desde esta rama).
- Dejar lista la evidencia para el video: eventos publicados, enlace único cambiando el estado del turno y tests en verde.
