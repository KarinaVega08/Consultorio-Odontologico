# Design — C-07 frontend-web (Etapa 1: base de Franco Castellarín)

## Context

El backend está completo y mergeado (`C-01` a `C-06`) y el frontend solo tiene `index.html`, `login.html` y `js/auth.js` con CSS inline y sin navegación común. El `CHANGES.md` define tres etapas: **Franco Castellarín** arma la base visual, maquetado y navegación (`feat/front-base`), **Mailén Claros** finiquita la lógica interactiva (`feat/front-completo`) y **Thiago Sinatra** revisa E2E y unifica en `main`. Este design cubre solo la Etapa 1 (comportamiento en `specs/frontend-web/spec.md`).

## Goals / Non-Goals

**Goals:**

- Un sistema de estilos único y reutilizable que unifique las 4 páginas.
- Navegación común (header/footer) con estado de sesión visible en todas las páginas.
- Maquetado de `reservar.html` y `mis-turnos.html` listo para que Mailén Claros agregue la lógica.
- Un módulo HTTP con token (`js/api.js`) que `auth.js` y las páginas nuevas consuman.
- `./gradlew test` sigue en verde; sin cambios de comportamiento en las APIs.

**Non-Goals:**

- Consultar `/api/availability`, crear/cancelar/reprogramar turnos, ni renderizar datos reales (Etapa 2 de Mailén Claros).
- Rehacer el registro/login/vinculación de Gmail (ya funcionan en `auth.js`).
- Interfaz de administración (profesionales, sillones, horarios, bloqueos, prestación) y pantallas de recepción.
- Frameworks JS, build de frontend, `package.json` ni TypeScript (el change manda HTML/CSS/JS estático).

## Decisions

### D1 — Estilos externos únicos (`css/styles.css`)

- Se crea `static/css/styles.css` con variables (`--primary`, `--secondary`, `--accent`, `--danger`, `--bg`, `--card-bg`, `--border`, `--text`, `--text-muted`), reset, tipografía, header/nav, footer, botones (`.btn`, `.btn-primary`, `.btn-secondary`, `.btn-block`), tarjetas, grid, formularios, alertas, badges por estado, tabla, estado vacío y media queries.
- Se elimina el `<style>` inline de `index.html` y `login.html`; las reglas específicas de login (tabs, perfil, banner de Gmail) se mueven al archivo compartido.

**Por qué**: coherencia visual, un solo lugar para ajustes y una base limpia para Mailén Claros. **Alternativa descartada**: seguir con CSS inline por página (duplicación y divergencia visual).

### D2 — Header/footer comunes y `js/nav.js`

- Cada página incluye `<header class="site-header">` con marca y enlaces (Inicio, Sacar Turno, Mis Turnos, entrada de cuenta) y `<footer class="site-footer">`.
- `js/nav.js` (tras `api.js`) ajusta la entrada de cuenta según `Api.isLoggedIn()`: *Iniciar sesión* o *Mi Cuenta* + botón *Cerrar Sesión* (limpia el token y navega a `/index.html`).
- La navegación es estática (sin plantillas): cada página repite el mismo marcado, lo que evita dependencias de build.

**Por qué**: es lo que exige el alcance ("navegación") y no requiere infraestructura extra. **Alternativa descartada**: componentes JS que inyecten el header (rompe la simplicidad y el "HTML maquetado" pedido).

### D3 — Módulo HTTP `js/api.js` y refactor de `auth.js`

- `js/api.js` expone `window.Api`: `getToken`, `setToken`, `clearToken`, `isLoggedIn`, `logout` y `request/get/post/put/delete` que agregan `Authorization: Bearer` si hay token, envían JSON cuando corresponde y devuelven `{ok, status, data}`.
- `auth.js` se refactoriza para usar `Api` en registro, login, vinculación de Gmail y restauración de sesión, **conservando los mismos ids de DOM y el mismo comportamiento observable** (mensajes, tabs, perfil).
- Se mantiene una sola clave de token (`consultorio_auth_token`) definida en `api.js`.

**Por qué**: las páginas nuevas (Etapa 2) necesitan requests autenticados sin duplicar lógica; el refactor evita dos implementaciones divergentes. **Alternativa descartada**: duplicar el fetch de `auth.js` en cada página (deuda técnica).

### D4 — Whitelist de páginas estáticas

- Se agregan `/reservar.html` y `/mis-turnos.html` al `requestMatchers` permitido de `SecurityConfig`.
- **No** se modifica ninguna otra regla: APIs, `/api/admin/**` y el módulo de notificaciones quedan igual.

**Por qué**: la navegación del browser no envía el header `Bearer`; sin esto las páginas nuevas devolverían 401/403 antes de renderizar. **Alternativa descartada**: mover las páginas a `/static/**` (rompería los enlaces definidos en `CHANGES.md`).

### D5 — Estructura de datos esperada (handoff a Mailén Claros)

- `reservar.html` expone `#profesional`, `#sillon`, `#prestacion`, `#fecha`, `#franjas` y `#reserva-alert`.
- `mis-turnos.html` expone `#turnos-list`, `#turnos-alert` y badges con clases `.badge.reservado|confirmado|cancelado|completado`.
- `AppointmentResponse` solo trae UUIDs; Mailén Claros decidirá cómo resolver nombres (posible uso de las listas de `/api/admin/**` para personal o endpoints de detalle).

### D6 — Verificación sin framework JS

- Verificación automatizada = `./gradlew test` en verde (Java) + `openspec validate --strict`.
- La revisión visual y el flujo E2E en navegador los hace Thiago Sinatra en la Etapa 3; esta etapa no agrega tests JS (no hay infraestructura de frontend en el repo).

## Risks / Trade-offs

- **[HTML duplicado entre páginas]** → aceptado: sin build ni templating, el header/footer se repite; el costo es menor que introducir una toolchain nueva a 2 días de entrega.
- **[Whitelist ampliada]** → se agregan solo 2 rutas de página estática (no APIs); el test de seguridad existente debe seguir en verde, lo que se verifica en el apply.
- **[Refactor de `auth.js`]** → riesgo de romper el login existente; mitigado conservando ids, mensajes y comportamiento, y cubriendo el escenario "login sigue funcionando" en la spec.
- **[Handoff con datos solo-UUID]** → Mailén Claros puede necesitar resolver nombres; queda documentado en D5 como punto de coordinación.

## Migration Plan

No aplica: son archivos estáticos nuevos o reestilizados. Rollback = revert del commit; no hay cambios de esquema ni de datos.

## Open Questions

- **Ubicación final del frontend**: `CHANGES.md` manda estáticos en `backend/src/main/resources/static/` (decisión adoptada); la KB propone `frontend/` con TypeScript. Unificar es trabajo post-entrega.
- **Nombres de profesional/prestación en "Mis Turnos"**: decisión de Mailén Claros en la Etapa 2 (D5).
