# Especificación — C-03 configuracion-consultorio

## Objetivo
Permitir al Administrador configurar los recursos del consultorio y el catálogo de prestaciones con duración variable.

## Requisitos funcionales
1. CRUD de profesionales (nombre y matrícula), sillones/boxes (nombre) y prestaciones (nombre, duración en minutos y descripción).
2. Alta, edición, listado y activación/desactivación de cada recurso.
3. Horarios de atención por profesional y día de semana (1-7), con `hora_fin > hora_inicio`.
4. Bloqueos por profesional y/o sillón/box, con `fecha_hora_fin > fecha_hora_inicio` y al menos una referencia.
5. Endpoints admin restringidos al rol ADMINISTRADOR.

## Criterios de aceptación (Dado / Cuando / Entonces)
- **CA-1 (feliz)**: Dado un Administrador autenticado, cuando crea una prestación "Obturación" con duración 45, entonces recibe 201 y la prestación queda activa y consultable.
- **CA-2 (error de negocio)**: Dado un Administrador, cuando intenta crear una prestación con duración 0, entonces recibe 400 con mensaje de regla de negocio y no se persiste nada.
- **CA-3 (borde)**: Dado un profesional existente, cuando se crea un horario con día 8 o con `hora_fin <= hora_inicio`, entonces recibe 400; y si el profesional no existe, recibe 404.
- **CA-4 (borde)**: Cuando se crea un bloqueo sin profesional ni sillón, entonces recibe 400; si solo tiene profesional, se acepta.
- **CA-5 (seguridad)**: Dado un usuario sin rol ADMINISTRADOR, cuando llama a cualquier endpoint admin, entonces recibe 403.
- **CA-6 (trazabilidad)**: Dado un profesional activo, cuando se desactiva, entonces sigue existiendo con `activo = false` (nunca borrado físico).

## Requisitos no funcionales
Java 21, Spring Boot 3, H2 en tests, DTOs con `@Valid`, `@ControllerAdvice`, SLF4J, datos ficticios (Ley 25.326), sin servicios externos.
