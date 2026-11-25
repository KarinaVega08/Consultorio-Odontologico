# Spec Delta — C-07 frontend-web

## Purpose

Entregar la base del frontend estático (servido por el backend): sistema de estilos unificado, navegación común con estado de sesión, maquetado de las páginas de reserva y de turnos, y un módulo HTTP con token listo para que la Etapa 2 construya la lógica interactiva. Esta spec cubre **solo la Etapa 1 (Franco Castellarín)**; la lógica de disponibilidad, reserva y cancelación se especifica en la continuación del mismo change (Etapa 2, Mailén Claros).

## ADDED Requirements

### Requirement: Sistema de estilos unificado compartido
El frontend SHALL servir un único archivo `css/styles.css` con la paleta, tipografía, botones, tarjetas, formularios, alertas, badges de estado, tabla y estilos responsive; las páginas SHALL no incluir CSS inline (`<style>`).

#### Scenario: Las páginas usan el mismo archivo de estilos (feliz)
- **GIVEN** que se cargan `index.html`, `login.html`, `reservar.html` y `mis-turnos.html`
- **WHEN** se inspecciona el HTML de cada página
- **THEN** todas referencian `/css/styles.css`
- **AND** ninguna contiene un bloque `<style>` inline

#### Scenario: Los componentes visuales están definidos en el archivo compartido (borde)
- **GIVEN** que se abre `css/styles.css`
- **WHEN** se busca la definición de botones, tarjetas, formularios, alertas, badges y tabla
- **THEN** todos ellos están definidos ahí (no hay reglas duplicadas en las páginas)

### Requirement: Navegación común con estado de sesión
Cada página SHALL incluir un header con la marca del consultorio y los enlaces *Inicio*, *Sacar Turno*, *Mis Turnos* y la entrada de cuenta, más un footer. La entrada de cuenta SHALL mostrar *Iniciar sesión* sin token en `localStorage` y *Mi Cuenta* junto con *Cerrar Sesión* cuando exista un token; cerrar sesión SHALL limpiar el token y volver al inicio.

#### Scenario: Navegación presente en todas las páginas (feliz)
- **GIVEN** que se abre cualquiera de las cuatro páginas
- **WHEN** se carga la página
- **THEN** el header contiene los enlaces a `/index.html`, `/reservar.html` y `/mis-turnos.html`
- **AND** el footer está presente

#### Scenario: La cuenta refleja el estado de sesión (borde)
- **GIVEN** que no hay token en `localStorage`
- **WHEN** se carga la página
- **THEN** la entrada de cuenta dice "Iniciar sesión" y apunta a `/login.html`

#### Scenario: Cerrar sesión limpia el token (borde)
- **GIVEN** que existe un token en `localStorage`
- **WHEN** se presiona "Cerrar Sesión"
- **THEN** se elimina `consultorio_auth_token` del `localStorage`
- **AND** se navega a `/index.html`, donde la cuenta vuelve a mostrar "Iniciar sesión"

### Requirement: Páginas de reserva y turnos alcanzables sin autenticación
`GET /reservar.html` y `GET /mis-turnos.html` SHALL responder `200 OK` sin token de autenticación (la página se sirve; el acceso a los datos depende de la Etapa 2).

#### Scenario: Navegación sin sesión no recibe 401/403 (feliz)
- **GIVEN** que un cliente no envía header `Authorization`
- **WHEN** solicita `GET /reservar.html` y `GET /mis-turnos.html`
- **THEN** ambas responden `200 OK` con su HTML

### Requirement: Maquetado de la página de reserva
`reservar.html` SHALL presentar un formulario de reserva con selección de profesional, sillón/box, prestación y fecha, y un contenedor identificado para renderizar las franjas horarias libres, más un área de mensajes. La página SHALL no ejecutar consultas de disponibilidad ni envíos de reserva (Etapa 2).

#### Scenario: Estructura de reserva lista para la lógica (feliz)
- **GIVEN** que se abre `GET /reservar.html`
- **WHEN** se inspecciona la página
- **THEN** existen campos para profesional, sillón/box, prestación y fecha
- **AND** existe un contenedor identificado para las franjas horarias y un área de mensajes

#### Scenario: Sin lógica interactiva en la Etapa 1 (borde)
- **GIVEN** que se abre `reservar.html`
- **WHEN** se revisa el JavaScript de la página
- **THEN** no hay llamadas a `/api/availability` ni a `/api/appointments` (pertenecen a la Etapa 2)

### Requirement: Maquetado del panel de turnos
`mis-turnos.html` SHALL presentar un panel con contenedor para la lista de turnos, fila/encabezado de columnas (inicio, fin, estado y acciones), badges de estado y un estado vacío visible.

#### Scenario: Panel de turnos con estado vacío (feliz)
- **GIVEN** que se abre `GET /mis-turnos.html` sin turnos
- **WHEN** se carga la página
- **THEN** se muestra el mensaje de estado vacío ("No tenés turnos reservados")

#### Scenario: Estructura de tabla lista para la lógica (borde)
- **GIVEN** que se inspecciona `mis-turnos.html`
- **WHEN** se revisa la estructura
- **THEN** existe un contenedor identificado para el listado y badges de estado predefinidos (`RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`)

### Requirement: Módulo HTTP con token reutilizable
El frontend SHALL exponer `js/api.js` con lectura/escritura/eliminación de `consultorio_auth_token` en `localStorage` y un método de request que agregue `Authorization: Bearer <token>` cuando exista, devolviendo `{ok, status, data}`. `auth.js` SHALL consumir este módulo.

#### Scenario: Request autenticado envía el Bearer (feliz)
- **GIVEN** que hay un token guardado en `localStorage`
- **WHEN** el módulo realiza un `GET` a una API protegida
- **THEN** la petición incluye el header `Authorization: Bearer <token>`

#### Scenario: Sin token no se envía Authorization (borde)
- **GIVEN** que no hay token en `localStorage`
- **WHEN** el módulo realiza una petición
- **THEN** no se incluye el header `Authorization`

#### Scenario: El login sigue funcionando tras el refactor (feliz)
- **GIVEN** que se abre `login.html`
- **WHEN** se envía un login válido
- **THEN** el token se guarda en `consultorio_auth_token` y se muestra el perfil (comportamiento previo conservado)

### Requirement: Sin regresión del backend
El frontend SHALL no alterar el comportamiento de las APIs; el único cambio de backend permitido es agregar `/reservar.html` y `/mis-turnos.html` a la whitelist de `SecurityConfig`, sin modificar el resto de las reglas de seguridad ni el módulo de notificaciones.

#### Scenario: La suite del backend sigue en verde (feliz)
- **GIVEN** que se ejecuta `./gradlew test` en `backend/`
- **WHEN** termina la suite
- **THEN** el build es `BUILD SUCCESSFUL` con 0 fallos

#### Scenario: Rutas protegidas siguen protegidas (seguridad)
- **GIVEN** que un cliente sin token solicita `GET /api/appointments` o `GET /api/admin/professionals`
- **THEN** el sistema responde `401 Unauthorized` o `403 Forbidden`
- **AND** la whitelist solo incorporó `/reservar.html` y `/mis-turnos.html`
