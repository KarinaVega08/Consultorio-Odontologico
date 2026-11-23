# Especificación — C-02 auth-pacientes

## Objetivo
Implementar autenticación y gestión de cuenta de pacientes: registro/login por DNI, vinculación única de Gmail de recuperación, recuperación de contraseña por token, reset de contraseña y blanqueo presencial.

## Requisitos funcionales del change
1. Registro de paciente con nombre real, DNI (7-8 dígitos único) y contraseña hasheada con BCrypt.
2. Login con DNI + contraseña, emitiendo token Bearer firmado con HMAC-SHA256.
3. Vinculación única de Gmail de recuperación (una sola vez después del primer login).
4. Recuperación de contraseña por token generado si el paciente tiene Gmail vinculado (respuesta genérica para no filtrar existencia de DNI).
5. Reset de contraseña con token único de un solo uso y validación de vencimiento.
6. Blanqueo presencial de contraseña por Recepcionista o Administrador (`/api/admin/password-reset` protegido).
7. Rutas públicas: registro, login, recuperación, reset. El resto requiere autenticación.

## Requisitos no funcionales
- Stack: Java 21, Spring Boot 3, Gradle, JPA/Hibernate, API REST.
- Seguridad: BCrypt para contraseñas, tokens HMAC-SHA256, sesiones STATELESS, CSRF deshabilitado.
- Cumplimiento Ley 25.326: cero datos reales de pacientes en repo, seeds o tests.
- Ningún secreto hardcodeado: JWT secreto leído de variable de entorno (`${JWT_SECRET:}`); si vacío, se genera clave aleatoria en memoria.

## Criterios de aceptación
- [ ] Registro valida formato y unicidad de DNI (7-8 dígitos)
- [ ] Login devuelve token Bearer y perfil del paciente
- [ ] Vinculación de Gmail solo una vez; segundo intento rechaza con 400
- [ ] Recuperación de contraseña devuelve 200 sin filtrar existencia de DNI
- [ ] Reset de contraseña valida token único de un solo uso y vencimiento
- [ ] Blanqueo presencial requiere rol RECEPCIONISTA o ADMINISTRADOR
- [ ] `./gradlew test` pasa en verde con H2 en memoria
