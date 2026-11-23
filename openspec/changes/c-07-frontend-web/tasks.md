# Tasks — C-07 frontend-web (Etapa 1: base de Caste)

> Checklist de trabajo para **Caste**. Rama: `feat/front-base` (solo este change).
> Referencias: `specs/frontend-web/spec.md` (qué) y `design.md` (cómo).
> La lógica interactiva es de **Mai** (Etapa 2, rama `feat/front-completo`) y la revisión/merge de **Thiago** (Etapa 3).

## 1. Coordinación y preparación

- [ ] 1.1 Avisar a **Mai** que `feat/front-base` queda lista para que finiquite la lógica (ids de DOM en `reservar.html`/`mis-turnos.html`, `Api` en `js/api.js`, handoff en design D5) — verificación: mensaje en el chat del equipo.
- [ ] 1.2 Avisar a **Thiago** que el apply incluye un cambio mínimo de whitelist en `SecurityConfig` — verificación: mensaje en el chat del equipo.
- [x] 1.3 Confirmar en verde la base antes de empezar — verificación: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`.

## 2. Ciclo OPSX — Explore y Propose

- [x] 2.1 Explore: relevar frontend, contratos de API y seguridad en `explore.md` — verificación: archivo presente con brechas detectadas.
- [x] 2.2 Propose: `proposal.md` con Why, What Changes y capacidad `frontend-web` — verificación: `openspec status --change c-07-frontend-web` marca `proposal` done.
- [x] 2.3 Spec: `specs/frontend-web/spec.md` con 7 requirements y 12 escenarios WHEN/THEN (feliz, borde, seguridad) — verificación: `openspec validate c-07-frontend-web --strict` sin errores.
- [x] 2.4 Design: `design.md` con decisiones D1-D6 — verificación: archivo presente con todas las secciones.
- [x] 2.5 Tasks: este archivo — verificación: `openspec status --change c-07-frontend-web` → todos los artefactos `done`.
- [x] 2.6 Subir la rama a `origin/feat/front-base` cuando Caste lo autorice (sin PR: la revisión es de Thiago) — verificación: `git ls-remote origin refs/heads/feat/front-base`.

## 3. Base visual (apply)

- [x] 3.1 Crear `static/css/styles.css` con variables, reset, header/nav, footer, botones, tarjetas, grid, formularios, alertas, badges por estado, tabla, estado vacío y responsive (D1) — verificación: las 4 páginas referencian `/css/styles.css` y ninguna tiene `<style>` inline.
- [x] 3.2 Reestilizar `index.html` sobre la base visual nueva, conservando el health check de `/api/health` — verificación: la landing carga con header, footer y las tarjetas de características.
- [x] 3.3 Reestilizar `login.html` conservando **todos** los ids que usa `auth.js` (`alert-box`, `auth-section`, `profile-section`, `tab-login`, `tab-register`, `login-form`, `register-form`, `gmail-form`, `gmail-link-box`, `logout-btn`, `prof-*`) — verificación: registro, login, vinculación de Gmail y logout siguen funcionando igual.

## 4. Maquetado de páginas (apply)

- [x] 4.1 Crear `static/reservar.html` con header/footer, formulario (profesional, sillón/box, prestación, fecha), contenedor `#franjas` y área `#reserva-alert`, **sin llamadas a APIs** (D5) — verificación: la página carga y tiene la estructura completa.
- [x] 4.2 Crear `static/mis-turnos.html` con header/footer, contenedor `#turnos-list`, tabla (inicio, fin, estado, acciones), badges de estado y estado vacío — verificación: muestra el mensaje de estado vacío al cargar.

## 5. Navegación y módulo HTTP (apply)

- [x] 5.1 Crear `static/js/api.js` con `Api` (token en `consultorio_auth_token`, `request/get/post/put/delete` con `Authorization: Bearer` y `{ok, status, data}`) — verificación: test manual de que un request con token envía el header y sin token no lo envía.
- [x] 5.2 Crear `static/js/nav.js` con la entrada de cuenta según sesión y cierre de sesión — verificación: sin token muestra "Iniciar sesión"; con token muestra "Mi Cuenta" y "Cerrar Sesión" limpia el token y navega al inicio.
- [x] 5.3 Refactorizar `auth.js` para consumir `Api` sin cambiar comportamiento — verificación: escenario "El login sigue funcionando tras el refactor" en verde.

## 6. Seguridad (apply)

- [x] 6.1 Agregar `/reservar.html` y `/mis-turnos.html` a la whitelist de `SecurityConfig` (sin tocar nada más) — verificación: `GET` de ambas páginas responde `200` sin token y `GET /api/appointments` sigue respondiendo `401`/`403`.

## 7. Verificación integral (verify)

- [x] 7.1 Correr la suite completa en verde: `./gradlew test` en `backend/` → `BUILD SUCCESSFUL`, 0 fallos, H2 en memoria — verificación: salida de Gradle.
- [x] 7.2 Revisar reglas duras: datos ficticios (Ley 25.326), cero secrets y cero CSS inline — verificación: sin `<style>` en las páginas ni credenciales en el diff.
- [x] 7.3 Revisar que el diff toque solo lo permitido (static + whitelist) — verificación: `git diff --name-only origin/main..HEAD`.

## 8. Cierre de esta rama

- [x] 8.1 Dejar la rama lista para revisión: commits locales hechos y `openspec validate` en verde — verificación: `git log origin/main..HEAD` muestra los commits de propose y de base; **push pendiente de autorización de Caste**.

## Workflow follow-up

- La Etapa 2 (**Mai**) parte de `feat/front-base` para agregar la lógica de disponibilidad, reserva y gestión.
- La Etapa 3 (**Thiago**) revisa E2E y unifica en `main`; la revisión, el merge y el archivado los hace él.
- Dejar lista la evidencia para el video: páginas navegables, tests en verde y el momento de "Ajustar" del propose.
