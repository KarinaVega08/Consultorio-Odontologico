# Explore — C-07 frontend-web (Etapa 1: base de Caste)

## Estado actual relevado (`2026-11-23`, desde `origin/main` @ `e5d9876`)

- **`main` en verde**: `./gradlew test` → `BUILD SUCCESSFUL` (70+ tests, H2 en memoria, sin servicios externos).
- **Backend completo y mergeado**: `C-01` (setup/base), `C-02` (auth con Bearer), `C-03` (configuración), `C-04` (motor de disponibilidad, archivado), `C-05` (gestión de turnos) y `C-06` (notificaciones, código base). El módulo de integración de `C-06` con turnos vive en la rama `feat/c06-integracion-turnos` y **todavía no está en `main`** (no bloquea esta etapa).
- **Frontend existente** (solo 3 archivos en `backend/src/main/resources/static/`):
  - `index.html`: landing con **CSS inline** (variables `--primary: #0284c7`, etc.), health check vía `fetch('/api/health')` y link a `login.html`.
  - `login.html`: registro, login y vinculación de Gmail, con **CSS inline** propio y `js/auth.js`.
  - `js/auth.js`: IIFE acoplada al DOM de `login.html`; ya usa la clave `consultorio_auth_token` en `localStorage`, envía `Authorization: Bearer`, hace logout y restaura sesión con `GET /api/patients/me`.
- **No existen**: `css/`, `reservar.html`, `mis-turnos.html`, `js/api.js`, `js/nav.js`.
- **Seguridad (`SecurityConfig`, `@Order(2)`)**: whitelist actual = `/api/health`, `/api/auth/**`, `/api/availability`, `/`, `/index.html`, `/login.html`, `/favicon.ico`, `/css/**`, `/js/**`, `/static/**`; todo lo demás `authenticated()`. **`/reservar.html` y `/mis-turnos.html` no están permitidas** → navegar a ellas sin token daría 401/403 (el browser no envía header Bearer en la navegación).
- **Sin tests de frontend**: no hay `package.json`, ni framework JS. La verificación es `./gradlew test` (backend) + revisión visual/E2E de Thiago.
- **Change `c-07-frontend-web` no existía** en `openspec/changes/` → se crea en esta rama.

## Contratos de API que la base debe respetar (los consume la Etapa 2 de Mai)

| Endpoint | Uso en el frontend | Forma relevante |
|---|---|---|
| `POST /api/auth/register`, `/api/auth/login` | registro/login (ya implementados en `auth.js`) | login → `{token, tokenType, patient:{id, ...}}` |
| `GET /api/patients/me` | perfil y `pacienteId` para reservar | `{id, nombreCompleto, dni, emailRecuperacion, requiereVincularGmail, fechaRegistro}` |
| `GET /api/availability` | franjas libres (Etapa 2) | query `profesionalId&fecha&prestacionId&sillonBoxId` → `{franjas:[{inicio, fin}]}` |
| `POST /api/appointments` | confirmar reserva (Etapa 2) | `{pacienteId, profesionalId, sillonBoxId, prestacionId, fechaHoraInicio}` → `201` con `AppointmentResponse` |
| `GET /api/appointments` | "Mis Turnos" (Etapa 2) | filtros `profesionalId&fecha&estado`; para rol `PACIENTE` el backend ya filtra solo sus turnos |
| `DELETE /api/appointments/{id}` | cancelar (Etapa 2) | devuelve el turno en estado `CANCELADO` |

Nota de dominio: `AppointmentResponse` expone **solo UUIDs** (`profesionalId`, `prestacionId`, `sillonBoxId`), no nombres; la presentación de nombres es responsabilidad de la Etapa 2.

## Componentes reutilizables

- Paleta y estilos ya existentes en `index.html`/`login.html` (inline) → se externalizan a `css/styles.css`.
- `js/auth.js` (flujo de registro/login/Gmail) → se refactoriza para consumir el nuevo `js/api.js` sin cambiar su comportamiento.
- API `GET /api/health` para el indicador de estado del backend.
- `SecurityConfig` y `GlobalExceptionHandler` (si se toca la whitelist, sin romper tests existentes).

## Brechas detectadas para la Etapa 1

1. **Sin sistema de estilos compartido**: cada página repite CSS inline → no hay coherencia visual ni componentes reutilizables.
2. **Sin navegación común**: no hay header/footer; `login.html` solo tiene un link de "volver".
3. **Sin páginas de reserva ni de turnos**: no existen `reservar.html` ni `mis-turnos.html`.
4. **Sin módulo HTTP reutilizable**: `auth.js` hace `fetch` a mano y está acoplado al DOM de login; las páginas nuevas no tendrían cómo autenticar requests.
5. **`/reservar.html` y `/mis-turnos.html` bloqueadas por seguridad** (no están en la whitelist).
6. **Estado de sesión invisible en la navegación**: no hay forma de mostrar "Iniciar sesión" vs "Mi Cuenta / Cerrar Sesión".

## Conclusión

La Etapa 1 es documentación + maquetado + módulo HTTP: se puede ejecutar **ahora** sin depender de otras ramas (el backend ya expone todo lo necesario). La lógica de disponibilidad, reserva, listado y cancelación queda **explícitamente fuera** de esta etapa (Etapa 2 de Mai) y la revisión/merge es de Thiago (Etapa 3).
