# Especificación — C-01 setup-base

## Objetivo
Levantar el esqueleto compilable del proyecto con todas las capas base configuradas, listas para que los cambios C-02, C-03 y C-04 se impliquen sobre ellas sin tocar la infraestructura.

## Requisitos funcionales del change
1. El proyecto compila con `./gradlew build` usando Java 21 y Spring Boot 3.
2. Se puede levantar una instancia local de Spring Boot conectando a PostgreSQL local.
3. Los tests automatizados corren con H2 en memoria sin necesidad de PostgreSQL levantado.
4. Existe un endpoint de healthcheck básico (opcional pero recomendado).
5. Los seeds de base contienen exclusivamente datos ficticios (no datos reales de pacientes).

## Requisitos no funcionales
- Stack: Java 21, Spring Boot 3.x, Gradle, JPA/Hibernate, API REST.
- Frontend: TypeScript Vanilla servido como archivos estáticos desde el mismo proceso.
- Base de datos: PostgreSQL local (ejecución), H2 en memoria (tests).
- Email: SMTP / Gmail (desarrollo: Mailpit o mock SMTP).
- Despliegue: entorno reproducible local, sin Docker obligatorio.
- Seguridad: nunca hardcodear secrets; usar variables de entorno.
- Cumplimiento Ley 25.326: cero datos reales de pacientes en repo, seeds o tests.

## Criterios de aceptación
- [ ] `./gradlew build` termina en verde.
- [ ] `./gradlew test` termina en verde con H2 en memoria.
- [ ] La aplicación arranca con `application.yml` y variables de entorno.
- [ ] Existe paquete `com.consultorio.domain`, `application`, `infrastructure`, `api`.
- [ ] Existe `application.yml` con placeholders para DB_URL, DB_USER, DB_PASSWORD, SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, MAILPIT_HOST, MAILPIT_PORT, APP_PORT.
- [ ] Se define RBAC por rol (Paciente, Recepcionista, Odontólogo, Administrador) aunque aún no haya endpoints protegidos.
- [ ] Los datos de seed son ficticios (ej. DNI 99.999.999, Juan Pérez).