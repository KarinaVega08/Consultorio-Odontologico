# Etapa 2 (Mailén Claros) — C-07 frontend-web

Rama: `feat/front-completo` (parte de `feat/front-base`, que es la Etapa 1 de Franco Castellarín).

## Qué se agregó

| Archivo | Responsabilidad |
|---------|-----------------|
| `static/js/catalog.js` | Carga `GET /api/catalog` y llena los `<select>` del formulario. |
| `static/js/reservar.js` | Consulta `GET /api/availability`, renderiza franjas y confirma con `POST /api/appointments`. |
| `static/js/mis-turnos.js` | Lista `GET /api/appointments` con badges y cancela con `DELETE /api/appointments/{id}`. |
| `api/controller/PublicCatalogController.java` | Catálogo público de profesionales, prestaciones y sillones (los de `/api/admin/**` son solo ADMIN). |
| `infrastructure/seed/DemoDataSeeder.java` | Datos ficticios para el perfil `demo` (Ley 25.326), idempotente. |
| `application-demo.yml` | H2 en memoria para ejecutar sin PostgreSQL. |
| `scripts/cargar-datos-demo.*` | Script para levantar backend + seed con un solo comando. |

## Cambios mínimos en archivos de la Etapa 1

- `reservar.html` y `mis-turnos.html`: inclusiones de los scripts nuevos; el sillón pasó a ser obligatorio porque `CreateAppointmentRequest.sillonBoxId` es `@NotNull`.
- `auth.js`: respeto de `?redirect=/reservar.html` (o `/mis-turnos.html`) después del login / vinculación de Gmail.
- `SecurityConfig.java`: `/api/catalog` agregado a la whitelist (público, solo lectura).
- `build.gradle`: H2 pasa de `testRuntimeOnly` a `runtimeOnly` para el perfil demo.

## Escenarios cubiertos

1. Cargar la página de reserva con el backend levantado → combos con datos del catálogo.
2. Elegir profesional/prestación/fecha → franjas libres (borde a borde permitido por C-04).
3. Sin sesión → redirección a `login.html?redirect=...` y regreso a la reserva.
4. Reservar → `POST /api/appointments` y refresco de franjas (la ocupada desaparece).
5. Mis Turnos → badges por estado y cancelación con confirmación; el horario se libera.
6. Sin datos → mensaje en la UI indicando que hay que correr el seed demo.

## Verificación

```bash
cd backend
./gradlew test
# Demo E2E:
../scripts/cargar-datos-demo.ps1   # Windows
```

Login ficticio: DNI `99999999`, contraseña `demo1234`.
