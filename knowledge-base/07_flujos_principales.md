# Flujos Principales

Cada flujo se documenta extremo a extremo, mostrando interacciones entre componentes.

## Flujo 1: Registro de paciente y primer login

**Disparador**: El paciente accede al sistema por primera vez y desea crear su cuenta.

**Actor**: Paciente

**Pasos**:
1. El paciente accede a la página de registro público.
2. El frontend envía `POST /api/auth/register` con nombre, DNI y contraseña.
3. El backend valida formato de DNI (7-8 dígitos) y unicidad en la base de datos.
4. Si el DNI ya existe, el backend rechaza el registro (mensaje de error).
5. Si el DNI es válido y único, el backend crea el registro del paciente con contraseña hasheada.
6. El backend responde éxito y el frontend redirige al login.
7. El paciente inicia sesión con DNI + contraseña (`POST /api/auth/login`).
8. Tras el primer login, si no hay Gmail vinculado, el sistema invita a vincularlo (una sola vez).

**Diagrama de secuencia** (ASCII):
```
Paciente → Frontend → Backend (validar DNI único) → DB
                                         ← respuesta OK
Frontend ← Backend (redirigir a login)
Paciente → Frontend → Backend (login DNI+pass) → DB
                                         ← sesión activa
```

**Casos de error**:
- DNI duplicado → mensaje: "Ya existe una cuenta con este DNI".
- DNI con formato inválido → mensaje: "El DNI debe tener 7 u 8 dígitos numéricos".

---

## Flujo 2: Reserva de turno online por el paciente

**Disparador**: El paciente desea reservar un turno.

**Actor**: Paciente (autenticado)

**Pasos**:
1. El paciente consulta disponibilidad (`GET /api/availability`) seleccionando profesional, fecha y prestación.
2. El backend calcula horarios libres considerando horarios de atención, bloqueos y turnos existentes (sin solapamiento por profesional ni por sillón/box).
3. El frontend muestra los horarios disponibles.
4. El paciente selecciona un horario y confirma la reserva (`POST /api/appointments`).
5. El backend valida anti-solapamiento nuevamente en el momento de creación (race condition guard).
6. El backend crea el turno en estado RESERVADO, calculando fecha_hora_fin = inicio + duracion_prestacion.
7. El backend envía email de confirmación al Gmail vinculado (SMTP/Gmail o Mailpit en dev).
8. El frontend confirma la reserva al paciente.

**Diagrama de secuencia** (ASCII):
```
Paciente → Frontend → Backend (consultar disponibilidad) → DB
                                         ← horarios libres
Frontend ← Backend (mostrar horarios)
Paciente → Frontend → Backend (crear turno) → DB (validar solapamiento)
                                         ← turno creado RESERVADO
Backend → SMTP/Gmail (email de confirmación)
Frontend ← Backend (reserva confirmada)
```

**Casos de error**:
- Solapamiento detectado al crear → mensaje: "El horario ya no está disponible, por favor elija otro".
- Fuera de horario laboral → mensaje: "El horario seleccionado no está dentro del horario de atención".

---

## Flujo 3: Creación de turno por personal del consultorio

**Disparador**: La recepcionista o el odontólogo desea agendar un turno para un paciente.

**Actor**: Recepcionista / Odontólogo

**Pasos**:
1. El personal busca al paciente por DNI.
2. El personal selecciona profesional, sillón/box, prestación y fecha/hora.
3. El backend valida que el profesional y el sillón/box estén disponibles en ese horario (sin solapamiento).
4. El backend crea el turno en estado RESERVADO.
5. El sistema notifica al paciente por email (si tiene Gmail vinculado).

**Casos de error**:
- Paciente no encontrado por DNI → mensaje: "No se encontró un paciente con ese DNI".
- Solapamiento → mensaje: "El profesional o el sillón ya tiene un turno en ese horario".

---

## Flujo 4: Cancelación de turno

**Disparador**: Un actor autorizado desea cancelar un turno existente.

**Actor**: Paciente, Recepcionista u Odontólogo

**Pasos**:
1. El actor solicita la cancelación del turno (`DELETE /api/appointments/{id}`).
2. El backend valida que el actor tenga permiso sobre el turno (es el paciente, o es recepcionista/administrador, o es el odontólogo asignado).
3. El backend cambia el estado a CANCELADO (conservando historial, nunca borrado físico).
4. El hueco del turno cancelado vuelve a figurar disponible de inmediato.
5. El sistema notifica por email al paciente (si corresponde).

**Casos de error**:
- Turno ya cancelado → mensaje: "El turno ya se encuentra cancelado".
- Sin permiso → mensaje: "No tiene permiso para cancelar este turno".

---

## Flujo 5: Blanqueo de contraseña presencial

**Disparador**: Un paciente olvida su contraseña y acude al consultorio.

**Actor**: Recepcionista o Administrador

**Pasos**:
1. El paciente solicita el blanqueo de contraseña en el consultorio.
2. La recepcionista verifica la identidad del paciente (DNI).
3. La recepcionista o administrador ejecuta el blanqueo (`POST /api/admin/password-reset`).
4. El sistema genera una contraseña temporal o permite establecer una nueva.
5. El paciente puede iniciar sesión con la nueva contraseña.

**Casos de error**:
- Paciente no encontrado → mensaje: "No se encontró un paciente con ese DNI".
- Sin permiso → mensaje: "Solo Recepcionista o Administrador pueden blanquear contraseñas".
