# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **Odontológico — Gestión de Turnos**.
> Cada change es atómico: un agente puede implementarlo en una sesión (~4-6 horas).
> **Leer este archivo antes de ejecutar cualquier `/opsx-propose`.**

> **Alcance y exclusiones (estricto):** Este CHANGES.md cubre únicamente el MVP definido en la knowledge-base y en las restricciones del usuario. Quedan **excluidos del MVP**: WhatsApp Business API, Mercado Pago, AFIP/ARCA, odontograma/periodontograma/radiografías y multi-sucursal. Nada de lo excluido se incluye en ningún change.

---

## Cómo usar este documento

1. **Identificar el change**: buscar el change con estado `[ ]` pendiente cuyas dependencias estén ya cumplidas (checkboxes marcados de sus dependencias).
2. **Leer la KB**: leer los archivos listados en "Leer antes" de ese change (fuente de verdad: `knowledge-base/`).
3. **Proponer**: ejecutar `/opsx-propose C-NN-nombre-del-change` con el scope detallado.
4. **Implementar y cerrar**: codificar, probar y marcar el checkbox del change como `[x]`.
5. **Archivar y avanzar**: ejecutar `/opsx-archive C-NN-nombre-del-change` y continuar con el siguiente change desbloqueado.

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
                └──► C-07 frontend-web     (depende de C-02, C-04, C-05, C-06)
```

---

### Paralelismo por fase (Distribución del equipo — 5 integrantes)

```
GATE 0: C-01 setup-base ✓     [Thiago — Líder técnico, sube base limpia a main y control general]
  → C-02 auth-pacientes                  [Mati + Thiago (Listo ✓) — feat/c02-registro-pacientes]
  → C-03 configuracion-consultorio       [Kari — feat/c03-config-consultorio]
  → C-04 disponibilidad-turnos           [Mai — feat/c04-disponibilidad-turnos]   ← FORK (Oleada 1: 3 ramas separadas)

GATE 1: C-02, C-03, C-04 ✓
  → C-05 gestion-turnos                  [Caste + Thiago (Listo ✓) — feat/c05-gestion-turnos]   (Oleada 2, Core MVP)

GATE 2: C-05 ✓
  → C-06 notificaciones                  [Kari + Caste — feat/c06-notificaciones (SMTP/Mailpit)]   (Change final backend)

GATE 3: C-06 ✓
  → C-07 frontend-web                    [Caste (Base) → Mai (Finiquitado) → Thiago (Unificación ✓)]
```

Cambios que pueden ejecutarse en paralelo sin colisiones de archivos (Oleada 1, GATE 1):
- **C-02 auth-pacientes (Mati + Thiago (Listo ✓))**: toca `backend/.../auth/` (Paciente, AuthController, AuthService, seguridad, registro/login/recuperación/blanqueo).
- **C-03 configuracion-consultorio (Kari)**: toca `backend/.../admin/` (Profesional, SillonBox, Prestacion, HorarioAtencion, Bloqueo + Admin CRUD).
- **C-04 disponibilidad-turnos (Mai)**: toca `backend/.../availability/` (AvailabilityController, AvailabilityService, lecturas de calendario).
- Ninguno escribe en los paquetes de los otros dos; solo leen entidades base creadas en C-01.

---

### Camino crítico (4 changes — mínimo irreducible)

`C-01 setup-base` → `C-02 auth-pacientes` → `C-05 gestion-turnos` → `C-06 notificaciones`

Nota: `C-03 configuracion-consultorio` y `C-04 disponibilidad-turnos` son ramas paralelas de igual longitud que también alimentan `C-05`; cualquiera de las tres (C-02/C-03/C-04) define el punto de desbloqueo de C-05, pero C-02 es la rama crítica por ser requisito de auth/RBAC.

---

### Plan de trabajo por integrante (Equipo completo — 5 integrantes)

| Fase | Change | Nombre | Responsable | Rama Git | Alcance asignado |
|---|---|---|---|---|---|
| **Fase 1** | **C-01** | `setup-base` | **Thiago** (Listo ✓) | `main` | Esqueleto base Java 21 + Spring Boot 3 + Gradle + PostgreSQL/H2, seguridad inicial, y control/revisión general |
| **Fase 2** (Oleada 1) | **C-02** | `auth-pacientes` | **Mati + Thiago (Listo ✓)** | `feat/c02-registro-pacientes` (mergeada en `main`) | Registro y login con DNI, vinculación única con Gmail, recuperación y reset de contraseña, blanqueo presencial |
| **Fase 2** (Oleada 1) | **C-03** | `configuracion-consultorio` | **Kari** | `feat/c03-config-consultorio` (mergeada en `main`) | CRUD profesionales, sillones/boxes, horarios, bloqueos, prestaciones con duración variable |
| **Fase 2** (Oleada 1) | **C-04** | `disponibilidad-turnos` | **Mai** | `feat/c04-disponibilidad-turnos` | Motor de disponibilidad y cálculo de franjas libres por profesional y sillón/box |
| **Fase 3** (Core MVP) | **C-05** | `gestion-turnos` | **Caste + Thiago (Listo ✓)** | `feat/c05-gestion-turnos` | Gestión de turnos sin solapamiento (profesional + sillón/box) y turnos consecutivos borde a borde |
| **Fase 4** (Cierre) | **C-06** | `notificaciones` | **Kari + Caste (Listo ✓)** | `feat/c06-notificaciones` (mergeada en `main`) | Recordatorios automáticos por email (SMTP/Mailpit) y enlaces únicos de confirmación/cancelación |
| **Fase 5** (Frontend) | **C-07** | `frontend-web` | **Caste + Mai + Thiago (Listo ✓)** | `feat/front-base` → `feat/front-completo` | Frontend completo: Caste arma la base visual, maquetado y navegación; Mai finiquita la lógica interactiva (reserva, disponibilidad y gestión); Thiago unifica en `main` |

> **Supervisión y control de calidad (Thiago):** Lidera el repositorio, revisa y aprueba los Pull Requests hacia `main`, asegurando la integración continua y la corrección general de la suite de pruebas.

---

## FASE 1 — Setup base y seguridad inicial

> Ejecutado primero por los líderes para subir la base limpia a `main`.

### [C-01] `setup-base`
- **Estado**: `[x]` completado
- **Scope**:
  - Esqueleto del proyecto: Java 21 + Spring Boot 3 + Gradle, módulo `backend/` + módulo `frontend/` (TypeScript Vanilla servido estático).
  - `build.gradle` / `settings.gradle` con dependencias: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, H2 (tests), Lombok, validation.
  - Estructura de carpetas por capas: `domain/`, `application/`, `infrastructure/`, `api/` bajo `com.consultorio`.
  - `application.yml` + variables de entorno (DB_URL, DB_USER, DB_PASSWORD, SMTP_*, MAILPIT_*, APP_PORT, FRONTEND_PATH).
  - Base de datos: PostgreSQL local para ejecución, H2 en memoria para tests (sin Docker obligatorio).
  - Seguridad inicial: Spring Security, RBAC por rol (Paciente, Recepcionista, Odontólogo, Administrador), CSRF deshabilitado, sesiones STATELESS, seeds solo con datos ficticios (Ley 25.326, RN-EX-01).
  - `BCryptPasswordEncoder` como bean `PasswordEncoder` para encoding de contraseñas.
  - `GlobalExceptionHandler` (`@ControllerAdvice`): manejo de errores de validación (`MethodArgumentNotValidException` → 400 con `fieldErrors`), `BusinessException` (400), `ResourceNotFoundException` (404), `RuntimeException` (500), `Exception` genérico (500).
  - Enums de dominio: `RolUsuario` (PACIENTE, RECEPCIONISTA, ODONTOLOGO, ADMINISTRADOR) y `EstadoTurno` (RESERVADO, CONFIRMADO, CANCELADO, COMPLETADO).
  - Excepciones de negocio: `BusinessException` y `ResourceNotFoundException`.
  - Entidad base: `BaseEntity` (`@MappedSuperclass`) con ID UUID, `activo`, `fechaCreacion`, `fechaActualizacion`.
  - API de health check: `GET /api/health` (200).
  - Landing estática: `backend/src/main/resources/static/index.html` servida desde el mismo proceso.
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
- **Estado**: `[x]` completado
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
- **Estado**: `[x]` completado
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
- **Estado**: `[x]` completado
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
- **Estado**: `[x]` completado
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
- **Estado**: `[x]` completado
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

> **Nota de integración (estado actual en `main`):** el **change C-06 está completo e integrado directamente en `main`** (adaptador SMTP, recordatorios configurables, enlaces únicos de confirmación/cancelación y fallback de envío; mergeado por Kari en `dabff4e` / `d42370c`), por eso su checkbox figura `[x]`. **No confundir con el cierre posterior `c-06-integracion-turnos`**: es un change **separado**, planificado en `origin/feat/c06-integracion-turnos`, que conectará C-05 con notificaciones (publicar `TurnoCreadoEvent`, `TurnoReprogramadoEvent` y `TurnoCanceladoEvent` desde `TurnoService`, y reemplazar el fallback no-op por un `TurnoParaRecordarPort` real). Ese cierre **ya está completado e integrado en `main`** (paquete `com.consultorio.integration.notification`, eventos publicados desde `TurnoService`, `TurnoRecordatorioAdapter` real y listeners de enlaces únicos), sin modificar el módulo `notification` ni la seguridad. Sigue siendo **fuera del alcance de C-06** y no modifica su estado.

## FASE 5 — Frontend web completo (interfaz de usuario final)

### [C-07] `frontend-web`
- **Estado**: `[x]` completado
- **Dinámica de equipo**: Caste (Etapa 1: base de diseño y maquetado) → Mai (Etapa 2: finiquitado con consumo de APIs y validaciones) → Thiago (Etapa 3: revisión general y unificación a `main`, delegada en Caste por disponibilidad del líder).
- **Scope**:
  - **Etapa 1 (Caste — rama `feat/front-base`)**:
    - Estilos unificados en `backend/src/main/resources/static/css/styles.css` (diseño moderno, tipografía, paleta de colores y componentes).
    - Barra de navegación común (header) y footer en todas las pantallas con accesos a: *Inicio*, *Sacar Turno*, *Mis Turnos* y *Login / Mi Cuenta / Cerrar Sesión*.
    - Maquetado HTML de `index.html` (landing page actualizada), `reservar.html` (formulario de reserva y contenedor para horarios) y `mis-turnos.html` (panel de listado de turnos).
    - Módulo de conexión HTTP enviando el token de autenticación guardado en `localStorage` (`consultorio_auth_token`).
  - **Etapa 2 (Mai — rama `feat/front-completo`)**:
    - Lógica de consulta de disponibilidad (`GET /api/availability`) con carga de profesionales, prestaciones y selección de fecha.
    - Renderizado interactivo de las franjas horarias libres y confirmación de la reserva con `POST /api/appointments`.
    - Lógica de "Mis Turnos" (`GET /api/appointments`) con visualización de turnos activos y cancelados con badges de estado.
    - Cancelación de turnos (`DELETE /api/appointments/{id}`) con confirmación y refresco automático.
    - Validaciones de experiencia de usuario (bloqueo de fechas pasadas, redirección si no hay sesión activa a `login.html`, y diseño responsive).
  - **Etapa 3 (Thiago — líder técnico)**:
    - Revisión del código final de Mai, prueba de flujo E2E completo en navegador y merge a `main` (realizado por Caste por delegación de Thiago).
- **Dependencias**: `C-02`, `C-04`, `C-05`, `C-06`
- **Governance**: MEDIO (interfaz de usuario del sistema)
- **Riesgo**: BAJO (el backend ya está 100% probado y funcional)
- **Leer antes**:
  - `knowledge-base/07_flujos_principales.md` §Flujo 1, §Flujo 2, §Flujo 3, §Flujo 4
  - `knowledge-base/08_arquitectura_propuesta.md` §Frontend estático

---

## Resumen de cambios

| Change | Nombre | Fase | Dependencias | Governance | Riesgo | Paralelizable con |
|--------|--------|------|--------------|------------|--------|-------------------|
| C-01 | setup-base | 1 | — | CRITICO | MEDIO | — (primero, líderes) |
| C-02 | auth-pacientes | 2 | C-01 | CRITICO | ALTO | C-03, C-04 |
| C-03 | configuracion-consultorio | 2 | C-01 | MEDIO | MEDIO | C-02, C-04 |
| C-04 | disponibilidad-turnos | 2 | C-01 | MEDIO | MEDIO | C-02, C-03 |
| C-05 | gestion-turnos | 3 | C-02, C-03, C-04 | CRITICO | ALTO | — (bloquea hasta que C-02/C-03/C-04 estén listos) |
| C-06 | notificaciones | 4 | C-05, C-02 | ALTO | MEDIO | — (último backend) |
| C-07 | frontend-web | 5 | C-02, C-04, C-05, C-06 | MEDIO | BAJO | — (Caste base → Mai finiquita → Thiago unificó) |

**Camino crítico**: `C-01 → C-02 → C-05 → C-06 → C-07` (5 changes).
**Gates de paralelismo**: 4 (GATE 0, GATE 1 FORK, GATE 2, GATE 3).
**Primer change recomendado**: `C-01 setup-base` → `/opsx-propose C-01-setup-base`.

Para arrancar: `/opsx-propose C-01-setup-base`
