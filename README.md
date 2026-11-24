# Consultorio-Odontológico
Sistema de gestión de turnos para consultorios odontológicos en Argentina.
Global Desarrollo de software 2026

## Stack

- **Backend:** Java 21 + Spring Boot 3 + Gradle + JPA/Hibernate + API REST
- **Frontend:** HTML/CSS/JavaScript estático servido por el mismo proceso backend (ver C-07 en `CHANGES.md`)
- **Base de datos:** PostgreSQL local (ejecución) / H2 en memoria (tests)
- **Email:** SMTP / Gmail (desarrollo: Mailpit o mock SMTP)
- **Despliegue:** entorno reproducible local, sin Docker obligatorio

## Requisitos previos

- Java 21 (Temurin/OpenJDK)
- Gradle 8.7 (se incluye el wrapper `./gradlew`)

## 1. Clonar el proyecto

```bash
git clone https://github.com/KarinaVega08/Consultorio-Odontologico.git
cd Consultorio-Odontologico
```

## 2. Configurar variables de entorno (opcional para PostgreSQL local)

Los tests automatizados corren en **H2 en memoria**, por lo que no es necesario instalar ni levantar PostgreSQL para ejecutar `./gradlew test`.

Para ejecutar la aplicación en modo local con PostgreSQL, crea un archivo `.env` en la raíz del módulo `backend/` (o exporta las variables en tu terminal):

```bash
# backend/.env  (ejemplo, no commitear valores reales)
DB_URL=jdbc:postgresql://localhost:5432/consultorio
DB_USER=consultorio_user
DB_PASSWORD=tu_contraseña
APP_PORT=8080

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu_usuario@gmail.com
SMTP_PASSWORD=tu_contraseña_de_aplicación

MAILPIT_HOST=localhost
MAILPIT_PORT=1025
```

Las variables tienen valores por defecto en `backend/src/main/resources/application.yml` apuntando a H2, por lo que son opcionales para desarrollo y tests.

> **Nunca commitees secrets reales.** Usa variables de entorno o un secret manager.

## 3. Ejecutar la suite de tests automatizados

Desde el módulo `backend/`:

```bash
cd backend
./gradlew test
```

- Los tests corren en **H2 en memoria** (sin requerir PostgreSQL ni servicios externos levantados).
- `./gradlew build` compila y ejecuta los tests.
- Resultado esperado: `BUILD SUCCESSFUL` con todos los tests en verde.

## Estructura del repositorio

```
backend/           # Módulo backend (Java 21 + Spring Boot 3 + Gradle)
openspec/          # Planificación OPSX (proposals, specs, designs, tasks, archive)
knowledge-base/    # Base de conocimiento del dominio (11 archivos .md)
docs/discovery/    # Informe de discovery y verificación de fuentes
CHANGES.md         # Roadmap de implementación (6 changes, C-01 a C-06)
AGENTS.md / CLAUDE.md  # Instrucciones para agentes
```

## Estado del proyecto

- **C-01 a C-06:** completados (backend, auth, configuración, disponibilidad, turnos y notificaciones).
- **C-07 frontend-web:** Etapa 1 (base visual) y Etapa 2 (lógica de reserva/turnos) ver `CHANGES.md`.

## Cómo correr la app con datos de prueba (sin PostgreSQL)

El perfil `demo` usa **H2 en memoria** y carga datos **ficticios** (DNI 99.999.999, "Juan Pérez").
No se conecta a ningún servicio externo. El seeder corre una sola vez por arranque.

**Windows (PowerShell):**
```powershell
.\scripts\cargar-datos-demo.ps1
```

**Linux / macOS:**
```bash
chmod +x scripts/cargar-datos-demo.sh
./scripts/cargar-datos-demo.sh
```

Si preferís hacerlo a mano:
```bash
cd backend
../gradlew bootRun --args='--spring.profiles.active=demo'
```

Hay **15 pacientes ficticios**, todos con contraseña `demo1234` (por ejemplo DNI `99999999` / Juan Pérez y DNI `30555123` / Romina Duarte, sin Gmail para probar la vinculación). El cartel del script lista los 15 DNIs.
Se siembran: **15 profesionales** con horarios variados, **2 sillones**, 3 prestaciones (30/45/60 min), horarios lun-vie 09:00-12:00 y 14:00-17:00, **15 turnos** en los 4 estados, un turno ocupado 09:30-10:00 y un bloqueo 10:00-10:30 en el próximo día hábil (Sillón 1).

> **Nota de despliegue:** `build.gradle` incluye H2 como `runtimeOnly` para que el perfil `demo` funcione sin PostgreSQL. Para un despliegue de producción, usar un perfil sin H2 y inyectar la conexión con `DB_URL`/`DB_USER`/`DB_PASSWORD` (nunca credenciales hardcodeadas).

## Puertos y páginas

- Backend + frontend estático: **http://localhost:8080** (variable `APP_PORT`).
- Landing: `/index.html`, login: `/login.html`, reserva: `/reservar.html`, turnos: `/mis-turnos.html`.
- No hay un servidor frontend separado: el mismo Spring Boot sirve el HTML/CSS/JS desde
  `backend/src/main/resources/static/`. No hace falta configurar CORS ni un proxy.

## APIs que consume el frontend

| Método | Endpoint | Uso | Autenticación |
|--------|----------|-----|---------------|
| `GET` | `/api/health` | Estado del backend (landing) | pública |
| `GET` | `/api/catalog` | Profesionales, prestaciones y sillones para los combos | pública |
| `GET` | `/api/availability?profesionalId=&fecha=&prestacionId=&sillonBoxId=` | Franjas libres | pública |
| `POST` | `/api/auth/register` · `/api/auth/login` | Alta e ingreso de pacientes | pública |
| `GET` | `/api/patients/me` | Perfil (para obtener el `pacienteId`) | Bearer |
| `POST` | `/api/appointments` | Confirmar reserva | Bearer |
| `GET` | `/api/appointments` | Mis turnos | Bearer |
| `DELETE` | `/api/appointments/{id}` | Cancelar (libera el horario) | Bearer |

## Licencia

Proyecto académico — UTN, Desarrollo de Software 2026.
