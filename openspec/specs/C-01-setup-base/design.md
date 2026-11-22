# Diseño técnico — C-01 setup-base

## Estructura de directorios
```
proyecto/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       └── main/
│           ├── java/com/consultorio/
│           │   ├── domain/          (entidades, reglas de negocio, interfaces de repositorio)
│           │   ├── application/     (casos de uso, servicios, DTOs)
│           │   ├── infrastructure/  (implementaciones JPA, adaptadores email, configuración)
│           │   └── api/             (controladores REST, DTOs de request/response)
│           └── resources/
│               ├── application.yml
│               └── data.sql         (seed data, solo datos ficticios)
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

## Stack y dependencias (build.gradle)
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- PostgreSQL driver (`org.postgresql:postgresql`)
- H2 (scope test)
- Lombok
- Spring Boot Test, JUnit 5 (scope test)

## Configuración externa
`application.yml` con propiedades bajo `spring.datasource`, `spring.jpa`, `server.port`, `smtp.*`, `mailpit.*`, `app.*`.
Variables de entorno mapeadas:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`
- `MAILPIT_HOST`, `MAILPIT_PORT`
- `APP_PORT`
- `FRONTEND_PATH`

## Seguridad inicial
- Spring Security configurado con RBAC por rol.
- Roles: Paciente, Recepcionista, Odontólogo, Administrador.
- BCrypt para encoding de contraseñas (preparado para C-02).
- Ningún secret hardcodeado.

## Base de datos
- PostgreSQL local para ejecución.
- H2 en memoria para tests (`spring.profiles.active=test`).
- `data.sql` con seed data ficticia únicamente.

## Decisiones de arquitectura
- Capas (Domain / Application / Infrastructure) dentro del backend.
- API REST bajo `/api/...`.
- DTOs para entrada/salida; nunca exponer entidades JPA directamente.
- Logging con SLF4J.