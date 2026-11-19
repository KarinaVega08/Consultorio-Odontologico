# Arquitectura Propuesta

## Patrones aplicados

| Patrón | Dónde se usa | Por qué |
|--------|--------------|---------|
| Capas (Domain / Application / Infrastructure) | Backend Spring Boot | Separación de responsabilidades, fácil de mantener y probar |
| API REST | Backend | Interfaz estándar para el frontend y futuros clientes |
| Repositorio (Spring Data JPA) | Acceso a datos | Abstracción de la capa de persistencia (PostgreSQL / H2) |
| DTOs | Capa de aplicación | Desacopla el modelo de dominio de la entrada/salida de API |
| Excepciones controladas | Backend | Respuestas de error consistentes y mensajes claros al cliente |
| Singleton de configuración | application.properties / YAML | Configuración centralizada de stack y conexiones |

## Estructura de directorios

```
proyecto/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       └── main/
│           ├── java/com/consultorio/
│           │   ├── domain/          (entidades, reglas de negocio, repositorios interfaces)
│           │   ├── application/     (casos de uso, servicios, DTOs)
│           │   ├── infrastructure/  (implementaciones JPA, adaptadores email, configuración)
│           │   └── api/             (controladores REST, DTOs de request/response)
│           └── resources/
│               ├── application.yml
│               └── data.sql (seed data, solo datos ficticios)
├── frontend/
│   ├── index.html
│   ├── src/
│   │   ├── app.js
│   │   ├── api/             (llamadas a fetch del backend)
│   │   ├── pages/           (login, registro, agenda, perfil, admin)
│   │   ├── components/      (formularios, tablas, modales)
│   │   └── styles/
│   └── package.json
├── tests/
│   ├── backend/             (pruebas unitarias con H2 en memoria)
│   └── frontend/            (pruebas de integración ligera)
└── README.md
```

## Seguridad

- **Autenticación**: Sesión basada en DNI + contraseña (token de sesión o JWT ligero, por definir en implementación).
- **Autorización**: RBAC por rol (Paciente, Recepcionista, Odontólogo, Administrador) verificado en cada endpoint sensible.
- **Validación de input**: Validación en backend de DNI (formato y unicidad), duraciones de prestación, horarios y anti-solapamiento.
- **Secrets management**: Variables de entorno para credenciales SMTP; nunca se commitean claves al repositorio.

## Variables de entorno

| Variable | Descripción | Ejemplo | Sensible (Y/N) |
|----------|-------------|---------|----------------|
| DB_URL | URL de conexión a PostgreSQL local | jdbc:postgresql://localhost:5432/consultorio | N |
| DB_USER | Usuario de base de datos | consultorio_user | N |
| DB_PASSWORD | Contraseña de base de datos | (secreto) | Y |
| SMTP_HOST | Servidor SMTP para envío de email | smtp.gmail.com | N |
| SMTP_PORT | Puerto SMTP | 587 | N |
| SMTP_USER | Usuario SMTP | (secreto) | Y |
| SMTP_PASSWORD | Contraseña SMTP | (secreto) | Y |
| MAILPIT_HOST | Host de Mailpit en desarrollo | localhost | N |
| MAILPIT_PORT | Puerto de Mailpit | 1025 | N |
| APP_PORT | Puerto de la aplicación Spring Boot | 8080 | N |
| FRONTEND_PATH | Ruta de archivos estáticos del frontend | src/main/resources/static | N |
