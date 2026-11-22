# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **Odontológico — Gestión de Turnos**.
> Cada change es atómico: un agente puede implementarlo en una sesión (~4-6 horas).
> **Leer este archivo antes de ejecutar cualquier `/opsx:propose`.**

> **Alcance y exclusiones (estricto):** Este CHANGES.md cubre únicamente el MVP definido en la knowledge-base y en las restricciones del usuario. Quedan **excluidos del MVP**: WhatsApp Business API, Mercado Pago, AFIP/ARCA, odontograma/periodontograma/radiografías y multi-sucursal. Nada de lo excluido se incluye en ningún change.

---

## Cómo usar este documento

1. **Identificar el change**: buscar el change con estado `[ ]` pendiente cuyas dependencias estén ya cumplidas (checkboxes marcados de sus dependencias).
2. **Leer la KB**: leer los archivos listados en "Leer antes" de ese change (fuente de verdad: `knowledge-base/`).
3. **Proponer**: ejecutar `/opsx:propose C-NN-nombre-del-change` con el scope detallado.
4. **Implementar y cerrar**: codificar, probar y marcar el checkbox del change como `[x]`.
5. **Archivar y avanzar**: ejecutar `/opsx:archive C-NN-nombre-del-change` y continuar con el siguiente change desbloqueado.

---

## Árbol de dependencias

```text
C-01 setup-base
├── C-02 auth-pacientes                    (solo depende de C-01)
├── C-03 configuracion-consultorio         (solo depende de C-01)
├── C-04 disponibilidad-turnos             (solo depende de C-01)
│   └── C-02, C-03 y C-04 no dependen entre sí (ramas paralelas de Oleada 1)
└──► C-05 gestion-turnos                   (depende de C-02, C-03, C-04)
        └──► C-06 notificaciones           (depende de C-05 y C-02)
```

---

### Paralelismo por fase

```
GATE 0: C-01 setup-base ✓     (líderes suben la base limpia a main)
  → C-02 auth-pacientes                  [Agente A — Thiago]
  → C-03 configuracion-consultorio       [Agente B — Kari]
  → C-04 disponibilidad-turnos           [Agente C — Mati]   ← FORK (Oleada 1: 3 ramas separadas, sin colisiones de archivos)

GATE 1: C-02, C-03, C-04 ✓
  → C-05 gestion-turnos                  [Agente A — Thiago]   (Oleada 2, core del MVP)

GATE 2: C-05 ✓
  → C-06 notificaciones                  [Agente B — Kari + Caste (SMTP/Mailpit)]   (Change final)
```

Cambios que pueden ejecutarse en paralelo sin colisiones de archivos (Oleada 1, GATE 1):
- **C-02 auth-pacientes**: toca `backend/.../auth/` (Paciente, AuthController, AuthService, seguridad, registro/login/recuperación/blanqueo).
- **C-03 configuracion-consultorio**: toca `backend/.../admin/` (Profesional, SillonBox, Prestacion, HorarioAtencion, Bloqueo + Admin CRUD).
- **C-04 disponibilidad-turnos**: toca `backend/.../availability/` (AvailabilityController, AvailabilityService, lecturas de calendario).
- Ninguno escribe en los paquetes de los otros dos; solo leen entidades base creadas en C-01.

---

### Camino crítico (4 changes — mínimo irreducible)

`C-01 setup-base` → `C-02 auth-pacientes` → `C-05 gestion-turnos` → `C-06 notificaciones`

Nota: `C-03 configuracion-consultorio` y `C-04 disponibilidad-turnos` son ramas paralelas de igual longitud que también alimentan `C-05`; cualquiera de las tres (C-02/C-03/C-04) define el punto de desbloqueo de C-05, pero C-02 es la rama crítica por ser requisito de auth/RBAC.

---

### Plan óptimo con 3 agentes

| Paso | Agente A (Backend Core — Thiago) | Agente B (Backend Aux — Kari) | Agente C (Frontend — Mati) |
|------|----------------------------------|-------------------------------|----------------------------|
| 1 | C-01 setup-base (esqueleto, Gradle, PostgreSQL/H2, carpetas, seguridad inicial) | — (prepara entorno H2, lee KB) | frontend shell + cliente API (parte de C-01) |
| 2 | C-02 auth-pacientes (registro/login/recuperación/blanqueo) | C-03 configuracion-consultorio (CRUD profesionales, sillones, horarios, bloqueos, prestaciones) | C-04 disponibilidad-turnos (vista de franjas libres + consumo API) |
| 3 | C-05 gestion-turnos (crear/reprogramar/cancelar, motor anti-solapamiento) | — | C-05 pantallas de agenda (crear/reprogramar/cancelar turno) |
| 4 | — | C-06 notificaciones (recordatorios SMTP/Mailpit + enlaces únicos de confirmación/cancelación) | — |

> Equipo completo (5 integrantes): Thiago y Mai lideran C-01 (setup base, suben base limpia a `main`). Oleada 1 ejecutada por Thiago (C-02), Kari (C-03) y Mati (C-04) en ramas separadas. Caste refuerza C-06 (SMTP/Mailpit, pruebas de entrega de correo).

---

## FASE 1 — Setup base y seguridad inicial

> Ejecutado primero por los líderes para subir la base limpia a `main`.

### [C-01] `setup-base`
- **Estado**: `[x]` completado
- **Scope**:
  - Esqueleto del proyecto: Java 21 + Spring Boot 3 + Gradle, módulo `backend/` + módulo `frontend/` (TypeScript Vanilla servido estático).
  - `build.gradle` / `settings.gradle` con dependencias: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, H2 (tests), Lombok, validation.
  - Estructura de carpetas por capas: `domain/`, `application/`, `infrastructure/`, `api/` bajo `com.consultorio`.
  - `application.yml` + variables de entorno (DB_URL, DB_USER, DB_PASSWORD, SMTP_*, MAILPIT_*, APP_PORT).
  - Base de datos: PostgreSQL local para ejecución, H2 en memoria para tests (sin Docker obligatorio).
  - Seguridad inicial: Spring Security, RBAC por rol (Paciente, Recepcionista, Odontólogo, Administrador), seeds solo con datos ficticios (Ley 25.326, RN-EX-01).
- **Dependencias**: ninguna
- **Governance**: CRITICO (modelos core e infraestructura de seguridad que todo lo demás referencia)
- **Riesgo**: MEDIO (configuración de conexión DB, versiones Java 21/SB 3, seguridad base)
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios, §Seguridad, §Variables de entorno
  - `knowledge-base/02_descripcion_general.md` §Stack tecnológico, §Integraciones externas
  - `knowledge-base/05_reglas_de_negocio.md` §RN-EX-01, §RN-EX-02

## FASE 2 — Oleada 1: cambios paralelos independientes (sin dependencias entre sí)

> Tres cambios que se ejecutan en ramas separadas sin pisar los mismos archivos. Cada uno depende solo de C-01.

### [C-02] `auth-pacientes`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Entidad `Paciente` (id UUID, nombre_completo, dni 7-8 dígitos único, password_hash, email_recuperacion opcional vinculado una sola vez, fecha_registro, activo).
  - `POST /api/auth/register` — valida formato y unicidad de DNI, crea paciente con contraseña hasheada.
  - `POST /api/auth/login` — autenticación DNI + contraseña; tras primer login invita a vincular Gmail (una sola vez).
  - `POST /api/auth/recover-password` — envío de enlace de recuperación al Gmail vinculado (SMTP/Mailpit).
  - `POST /api/auth/reset-password` — restablecimiento con token único.
  - `POST /api/admin/password-reset` — blanqueo presencial de contraseña (solo Recepcionista o Administrador).
  - Rutas públicas: registro, login, recuperación; el resto requiere autenticación.
  - Tests: registro DNI duplicado, DNI inválido, login exitoso/fallido.
- **Dependencias**: `C-01`
- **Governance**: CRITICO (autenticación y datos de seguridad)
- **Riesgo**: ALTO (race condition en registro de DNI único, hashing de contraseñas, tokens de recuperación)
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md` §RBAC, §Rutas públicas
  - `knowledge-base/05_reglas_de_negocio.md` §RN-AU-01 a RN-AU-05
  - `knowledge-base/04_modelo_de_datos.md` §Paciente
  - `knowledge-base/07_flujos_principales.md` §Flujo 1, §Flujo 5

### [C-03] `configuracion-consultorio`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Entidades `Profesional`, `SillonBox`, `Prestacion` (con `duracion_minutos` > 0), `HorarioAtencion`, `Bloqueo`.
  - CRUD de profesionales (alta/edición/activar-desactivar, nombre y matrícula).
  - CRUD de sillones/boxes (nombre, activar/desactivar).
  - Catálogo de prestaciones con duración variable en minutos (ej. Consulta 30, Obturación 45, Endodoncia 60).
  - Horarios de atención por profesional y día de semana; bloqueos por profesional y/o sillón/box.
  - Endpoints admin: `POST /api/admin/professionals`, `POST /api/admin/consulting-rooms`, `POST /api/admin/services`, horarios y bloqueos (solo Administrador, RN-TU-05).
  - Tests: CRUD de cada recurso, activación/desactivación, duración > 0.
- **Dependencias**: `C-01`
- **Governance**: MEDIO (CRUD de configuración con RBAC de administrador)
- **Riesgo**: MEDIO (validación de horarios y bloqueos, consistencia de entidades referenciadas)
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Profesional, §SillonBox, §Prestacion, §HorarioAtencion, §Bloqueo
  - `knowledge-base/06_funcionalidades.md` §Épica 2 (US-004 a US-007)
  - `knowledge-base/03_actores_y_roles.md` §RBAC (Administrador)
  - `knowledge-base/05_reglas_de_negocio.md` §RN-TU-05

### [C-04] `disponibilidad-turnos`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - `GET /api/availability` — consulta de horarios disponibles para un profesional, sillón/box, fecha y prestación.
  - Cálculo de franjas libres considerando: horarios de atención del profesional, bloqueos (profesional y sillón), turnos existentes, duración de la prestación.
  - La duración del bloque se deriva de `duracion_minutos` de la prestación seleccionada.
  - Validación de que el turno quepa completo dentro del horario laboral y sin chocar con bloqueos.
  - Lectura de entidades creadas en C-03 (no escribe en ellas; solo lectura).
  - Tests: franjas libres con bloqueos, fuera de horario, prestaciones de distintas duraciones.
- **Dependencias**: `C-01`
- **Governance**: MEDIO (flujo con estado y cálculo de disponibilidad)
- **Riesgo**: MEDIO (consistencia entre franjas calculadas y turnos reales; debe estar sincronizado con C-05)
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` (entidades de consulta: Profesional, SillonBox, Prestacion, Turno, Bloqueo, HorarioAtencion)
  - `knowledge-base/06_funcionalidades.md` §US-008
  - `knowledge-base/07_flujos_principales.md` §Flujo 2 (pasos 2-4)
  - `knowledge-base/05_reglas_de_negocio.md` §RN-TU-03

## FASE 3 — Oleada 2: core del MVP (candidato principal para el ciclo OPSX completo)

### [C-05] `gestion-turnos`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Entidad `Turno` (id UUID, FKs a Paciente/Profesional/SillonBox/Prestacion, fecha_hora_inicio, fecha_hora_fin calculada = inicio + duracion_prestacion, estado enum RESERVADO/CONFIRMADO/CANCELADO/COMPLETADO, creado_por, fechas de auditoría).
  - `POST /api/appointments` — creación de turno (paciente online y personal del consultorio) con validación anti-solapamiento por profesional y por sillón/box en el momento de creación (race-condition guard).
  - `PUT /api/appointments/{id}` — reprogramación de turno; libera el hueco anterior inmediatamente; respeta anti-solapamiento.
  - `DELETE /api/appointments/{id}` — cancelación a estado CANCELADO (historial conservado, nunca borrado físico); hueco disponible de inmediato.
  - `GET /api/appointments` — listado filtrado por profesional, fecha, estado.
  - Caso borde: turnos consecutivos borde a borde (fin = inicio del siguiente) permitidos.
  - Restricción de exclusión en BD (PostgreSQL `EXCLUDE USING gist`) o trigger para profesional y sillón/box por rango de tiempo (IN-04).
  - Tests: creación exitosa, rechazo por solapamiento profesional, rechazo por solapamiento sillón, turnos consecutivos borde a borde, cancelación libera hueco.
- **Dependencias**: `C-02`, `C-03`, `C-04`
- **Governance**: CRITICO (anti-solapamiento, datos de seguridad del turno, concurrencia)
- **Riesgo**: ALTO (race conditions en reservas simultáneas, estrategia de bloqueo en BD, turno consecutivo borde a borde)
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Turno
  - `knowledge-base/06_funcionalidades.md` §Épica 3 (US-009 a US-012)
  - `knowledge-base/07_flujos_principales.md` §Flujo 2, §Flujo 3, §Flujo 4
  - `knowledge-base/05_reglas_de_negocio.md` §RN-TU-01 a RN-TU-04
  - `knowledge-base/10_preguntas_abiertas.md` §IN-04 estrategia de bloqueo en BD

## FASE 4 — Notificaciones (change final)

### [C-06] `notificaciones`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Adaptador de envío de email por SMTP (Gmail en prod, Mailpit/mock en desarrollo); credenciales por variables de entorno, nunca commiteadas.
  - Recordatorios automáticos por email al Gmail vinculado del paciente con antelación configurable (valor por defecto 24h, configurable por Administrador, IN-02).
  - Email de confirmación al crear turno (US-009 CA-3) y notificación al cancelar/reprogramar.
  - Enlace único de confirmación de asistencia y enlace único de cancelación (confirmación/cancelación mediante enlace único).
  - Programación de recordatorios (task scheduler de Spring) y manejo de fallos de envío sin bloquear el flujo de turnos.
  - Tests: envío de recordatorio con Mailpit, enlace único de confirmación/cancelación, fallback si SMTP caído.
- **Dependencias**: `C-05`, `C-02`
- **Governance**: ALTO (sistema de notificaciones)
- **Riesgo**: MEDIO (configuración SMTP/Mailpit, unicidad de enlaces, entrega de correo, antelación de recordatorio por definir)
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-013, §Épica 4
  - `knowledge-base/05_reglas_de_negocio.md` §RN-NT-01, §RN-NT-02
  - `knowledge-base/08_arquitectura_propuesta.md` §Seguridad (secrets SMTP)
  - `knowledge-base/02_descripcion_general.md` §Integraciones externas (SMTP/Mailpit)
  - `knowledge-base/10_preguntas_abiertas.md` §IN-02 horas de antelación para recordatorio

---

## Resumen de cambios

| Change | Nombre | Fase | Dependencias | Governance | Riesgo | Paralelizable con |
|--------|--------|------|--------------|------------|--------|-------------------|
| C-01 | setup-base | 1 | — | CRITICO | MEDIO | — (primero, líderes) |
| C-02 | auth-pacientes | 2 | C-01 | CRITICO | ALTO | C-03, C-04 |
| C-03 | configuracion-consultorio | 2 | C-01 | MEDIO | MEDIO | C-02, C-04 |
| C-04 | disponibilidad-turnos | 2 | C-01 | MEDIO | MEDIO | C-02, C-03 |
| C-05 | gestion-turnos | 3 | C-02, C-03, C-04 | CRITICO | ALTO | — (bloquea hasta que C-02/C-03/C-04 estén listos) |
| C-06 | notificaciones | 4 | C-05, C-02 | ALTO | MEDIO | — (último) |

**Camino crítico**: `C-01 → C-02 → C-05 → C-06` (4 changes).
**Gates de paralelismo**: 3 (GATE 0, GATE 1 FORK, GATE 2).
**Primer change recomendado**: `C-01 setup-base` → `/opsx:propose C-01-setup-base`.

Para arrancar: `/opsx:propose C-01-setup-base`
