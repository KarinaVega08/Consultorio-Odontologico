# C-02 auth-pacientes Specification

## Purpose

Implementar autenticación y gestión de cuenta de pacientes: registro y login por DNI, vinculación única de Gmail de recuperación, recuperación y reset de contraseña por token, y blanqueo presencial.

## Requirements

### Requirement: Registro de paciente con DNI y BCrypt
El sistema SHALL registrar pacientes con nombre, DNI de 7 a 8 dígitos único y contraseña hasheada con BCrypt, rechazando DNIs inválidos o duplicados.

#### Scenario: Registro válido
- **WHEN** se registra un paciente con DNI ficticio no repetido y contraseña
- **THEN** se crea el perfil y la contraseña queda hasheada

#### Scenario: DNI duplicado
- **WHEN** se registra un paciente con un DNI ya existente
- **THEN** el sistema rechaza la operación con error de validación

### Requirement: Login con token Bearer
El sistema SHALL autenticar con DNI y contraseña, emitiendo un token Bearer firmado con HMAC-SHA256 y devolviendo el perfil del paciente; las sesiones SHALL ser STATELESS y CSRF SHALL estar deshabilitado.

#### Scenario: Login exitoso
- **WHEN** un paciente envía su DNI y contraseña correctos
- **THEN** recibe un token Bearer y su perfil

#### Scenario: Credenciales inválidas
- **WHEN** un paciente envía credenciales incorrectas
- **THEN** el sistema rechaza el acceso sin revelar cuál dato falló

### Requirement: Vinculación única de Gmail
El sistema SHALL permitir vincular una dirección @gmail.com de recuperación una sola vez, rechazando intentos posteriores.

#### Scenario: Primera vinculación
- **WHEN** el paciente vincula su Gmail por primera vez
- **THEN** queda registrado y se usa para recuperación

#### Scenario: Segundo intento
- **WHEN** el paciente intenta vincular otro Gmail
- **THEN** el sistema responde `400` y no modifica el correo existente

### Requirement: Recuperación y reset con token de un solo uso
El sistema SHALL generar un token de recuperación solo si hay Gmail vinculado, respondiendo de forma genérica para no filtrar la existencia del DNI; el token SHALL ser de un solo uso y expirar.

#### Scenario: Recuperación sin filtrar existencia
- **WHEN** se solicita recuperación con un DNI inexistente
- **THEN** el sistema responde `200` con el mismo mensaje que para un DNI existente

#### Scenario: Reset inválido
- **WHEN** se intenta reutilizar un token ya usado o vencido
- **THEN** el sistema rechaza el cambio de contraseña

### Requirement: Blanqueo presencial y rutas públicas
El endpoint `/api/admin/password-reset` SHALL requerir rol RECEPCIONISTA o ADMINISTRADOR. Registro, login, recuperación y reset SHALL ser públicos; el resto SHALL requerir autenticación.

#### Scenario: Blanqueo sin permisos
- **WHEN** un paciente intenta acceder al blanqueo presencial
- **THEN** el sistema responde `403 Forbidden`

#### Scenario: Ruta protegida sin token
- **WHEN** un cliente sin token solicita una ruta privada
- **THEN** el sistema responde `401` o `403`

### Requirement: Sin secretos hardcodeados
El secreto de firma SHALL leerse de una variable de entorno; si está vacío, SHALL generarse en memoria. El repositorio SHALL cumplir la Ley 25.326 con datos ficticios.

#### Scenario: Configuración segura
- **WHEN** se inspecciona la configuración y los tests
- **THEN** no hay secretos fijos ni datos reales de pacientes
