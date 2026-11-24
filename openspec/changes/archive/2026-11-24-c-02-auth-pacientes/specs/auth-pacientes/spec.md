# Especificación — C-02 auth-pacientes

## Requisitos funcionales
1. **Registro (`POST /api/auth/register`)**: recibe `nombreCompleto`, `dni` (7 u 8 dígitos numéricos) y `password` (mínimo 6 caracteres). Valida formato y unicidad de `dni`. Almacena la contraseña hasheada con BCrypt. Nunca devuelve `passwordHash`.
2. **Login (`POST /api/auth/login`)**: recibe `dni` y `password`. Verifica existencia de paciente activo y coincidencia de contraseña. Devuelve token Bearer, datos básicos del paciente y `requiereVincularGmail` (`true` si `emailRecuperacion == null`).
3. **Perfil (`GET /api/patients/me`)**: requiere token Bearer válido. Devuelve el DTO del paciente autenticado.
4. **Vinculación única de Gmail (`POST /api/patients/me/recovery-email`)**: requiere token Bearer válido. Recibe `email` (`@gmail.com`). Si el paciente no tiene `emailRecuperacion`, lo guarda y devuelve el perfil actualizado (`requiereVincularGmail = false`). Si ya tiene uno vinculado, rechaza la operación.
5. **Vista Login (`static/login.html`)**: permite registrarse, iniciar sesión con DNI + contraseña y vincular Gmail si aún no fue vinculado.

## Escenarios verificables (Dado / Cuando / Entonces)

### Escenario 1 (Feliz): Registro válido y primer login con invitación a vincular Gmail
- **Dado** que no existe un paciente registrado con el DNI `"99999991"`
- **Cuando** envía `POST /api/auth/register` con `nombreCompleto = "Juan Pérez"`, `dni = "99999991"`, `password = "ClaveSegura123"`
- **Entonces** el sistema responde `201 Created` con el DTO del paciente (sin `passwordHash`) y `requiereVincularGmail = true`
- **Y cuando** luego envía `POST /api/auth/login` con `dni = "99999991"` y `password = "ClaveSegura123"`
- **Entonces** el sistema responde `200 OK` con un token Bearer válido y `requiereVincularGmail = true`.

### Escenario 2 (Error de negocio): Registro rechazado por DNI duplicado
- **Dado** que ya existe un paciente activo con el DNI `"99999992"`
- **Cuando** otro usuario envía `POST /api/auth/register` con `dni = "99999992"`
- **Entonces** el sistema responde `400 Bad Request` con mensaje `"Ya existe una cuenta con este DNI"`.

### Escenario 3 (Error de validación): Registro rechazado por DNI con formato inválido
- **Dado** un payload de registro con `dni = "123"` (menos de 7 dígitos) o `"1234ABCD"` (no numérico)
- **Cuando** envía `POST /api/auth/register`
- **Entonces** el sistema responde `400 Bad Request` con `fieldErrors` indicando que el DNI debe tener 7 u 8 dígitos numéricos.

### Escenario 4 (Error de negocio): Login fallido por contraseña incorrecta
- **Dado** un paciente registrado con DNI `"99999993"` y contraseña `"ClaveValida1"`
- **Cuando** envía `POST /api/auth/login` con `dni = "99999993"` y `password = "Incorrecta"`
- **Entonces** el sistema responde `401 Unauthorized` y no emite token.

### Escenario 5 (Feliz + Borde): Vinculación única de Gmail y rechazo en segundo intento
- **Dado** un paciente autenticado con DNI `"99999994"` cuyo `emailRecuperacion` es `null`
- **Cuando** envía `POST /api/patients/me/recovery-email` con `email = "juan.perez.test@gmail.com"` y su token Bearer
- **Entonces** el sistema responde `200 OK` con `emailRecuperacion = "juan.perez.test@gmail.com"` y `requiereVincularGmail = false`
- **Y cuando** el mismo paciente vuelve a enviar `POST /api/patients/me/recovery-email` con otro correo `"otro.test@gmail.com"`
- **Entonces** el sistema responde `400 Bad Request` indicando que el correo de recuperación ya fue vinculado y no puede modificarse.

### Escenario 6 (Seguridad): Acceso denegado sin autenticación
- **Dado** un cliente sin token Bearer
- **Cuando** solicita `GET /api/patients/me` o `POST /api/patients/me/recovery-email`
- **Entonces** el sistema responde `401 Unauthorized` o `403 Forbidden`.
