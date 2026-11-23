# Proposal — C-07 frontend-web

## Why

El backend está completo y en verde (`C-01` a `C-06`), pero **la interfaz de usuario no existe**: solo hay una landing y una página de login con CSS inline, sin navegación común, sin página de reserva y sin panel de turnos. Sin una base visual coherente no hay forma de demostrar los flujos del producto (reserva, disponibilidad, gestión) ni de que Mai pueda construir la lógica interactiva sobre una estructura estable. Este change abre la **Fase 5 (frontend)** y entrega la **Etapa 1: base visual, maquetado y navegación** a cargo de **Caste**; la lógica interactiva es de Mai (Etapa 2) y la revisión/unificación es de Thiago (Etapa 3).

## What Changes

- **Sistema de estilos unificado** en `backend/src/main/resources/static/css/styles.css`: variables de color, tipografía, botones, tarjetas, formularios, alertas, badges de estado, tabla y responsive. Se elimina el CSS inline de `index.html` y `login.html`.
- **Navegación común**: header con marca y enlaces *Inicio*, *Sacar Turno*, *Mis Turnos* y cuenta (*Iniciar sesión* / *Mi Cuenta* + *Cerrar Sesión*), más footer, presentes en todas las páginas.
- **Maquetado de páginas**:
  - `index.html` (landing actualizada sobre la base visual nueva),
  - `reservar.html` (formulario de reserva y contenedor para las franjas horarias),
  - `mis-turnos.html` (panel de listado de turnos con badges de estado y estado vacío),
  - `login.html` (reestilizado, conservando todos los ids que usa `auth.js`).
- **Módulo de conexión HTTP** `js/api.js`: lectura/escritura del token en `localStorage` (`consultorio_auth_token`), `apiFetch` con header `Authorization: Bearer` y manejo uniforme de respuestas. `auth.js` se refactoriza para consumirlo sin cambiar su comportamiento.
- **Navegación según sesión** `js/nav.js`: muestra *Iniciar sesión* sin token y *Mi Cuenta* + *Cerrar Sesión* con token (limpia el token y vuelve al inicio).
- **Ajuste mínimo de seguridad**: `/reservar.html` y `/mis-turnos.html` pasan a la whitelist de `SecurityConfig` (la navegación del browser no puede enviar el header Bearer).
- **Sin lógica de negocio en esta etapa**: no se consultan APIs de disponibilidad, no se crean ni cancelan turnos desde la interfaz (Etapa 2).

## Capabilities

### New Capabilities

- `frontend-web`: base visual, maquetado y navegación del frontend estático servido por el backend, con módulo HTTP con token y páginas para reserva y turnos listas para la lógica interactiva.

### Modified Capabilities

<!-- Ninguna. El backend no cambia de comportamiento salvo la whitelist de páginas estáticas (permitAll), que no altera APIs ni reglas de negocio. -->

## Impact

- **Archivos nuevos**: `backend/src/main/resources/static/css/styles.css`, `static/reservar.html`, `static/mis-turnos.html`, `static/js/api.js`, `static/js/nav.js`.
- **Archivos modificados**: `static/index.html`, `static/login.html`, `static/js/auth.js` (refactor mínimo), `infrastructure/config/SecurityConfig.java` (whitelist de 2 páginas).
- **Dependencias**: backend mergeado en `main` (`C-01` a `C-06`); no depende de ramas abiertas (`feat/c06-integracion-turnos` no bloquea).
- **Tests**: `./gradlew test` debe seguir en verde (no hay tests JS; la revisión visual/E2E es de Thiago en la Etapa 3).
- **Entrega**: la Etapa 2 de Mai parte de `feat/front-base` para agregar la lógica interactiva; la Etapa 3 de Thiago unifica en `main`.

## Leer antes

- `CHANGES.md` — §FASE 5 `[C-07] frontend-web` (alcance por etapas)
- `knowledge-base/07_flujos_principales.md` — §Flujo 1, §Flujo 2, §Flujo 3, §Flujo 4
- `knowledge-base/08_arquitectura_propuesta.md` — §Frontend estático, §Estructura de directorios
- `knowledge-base/03_actores_y_roles.md` — §Rutas públicas
