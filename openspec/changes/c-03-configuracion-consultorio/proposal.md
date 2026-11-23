# Proposal — C-03 Configuración del Consultorio

## 1. Contexto
`C-01` dejó el esqueleto Spring Boot con seguridad inicial y manejo global de errores. El MVP requiere que el Administrador pueda configurar profesionales, sillones/boxes, prestaciones con duración variable, horarios de atención y bloqueos. Este cambio es rama paralela de `C-02` y `C-04` y solo depende de `C-01`.

## 2. Motivación
Sin esta configuración no existen los recursos que `C-04` consulta ni los que `C-05` asigna a un turno. Además, la duración de la prestación es la que determina el fin del turno (RN-TU-03), por lo que debe ser siempre un valor positivo.

## 3. Alcance (In-Scope)
- Entidades `Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion` y `Bloqueo` sobre `BaseEntity` (baja lógica vía `activo`).
- CRUD admin: alta, edición, listado y activar/desactivar de cada recurso.
- Catálogo de prestaciones con `duracion_minutos > 0`.
- Horarios por profesional y día de semana; bloqueos por profesional y/o sillón.
- Endpoints bajo `/api/admin/**` restringidos a `ADMINISTRADOR`.
- Tests automatizados con H2 (repositorio, servicio y controlador).

## 4. Fuera de Alcance
- Registro/login de pacientes (`C-02`).
- Cálculo de franjas libres (`C-04`) y gestión del ciclo de vida del turno (`C-05`).
- Notificaciones (`C-06`).
- WhatsApp, Mercado Pago, AFIP/ARCA, odontograma, multi-sucursal.

## 5. Reglas relacionadas
RN-TU-03, RN-TU-05, RN-EX-01, RN-EX-02.

## 6. Criterios de éxito
`./gradlew test` en verde con H2; endpoints protegidos por rol ADMINISTRADOR; ninguna entidad JPA expuesta directamente; datos de prueba exclusivamente ficticios.
