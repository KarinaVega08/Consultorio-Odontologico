# Proposal — C-04 Disponibilidad de Turnos

## 1. Contexto y Resumen Ejecutivo

El MVP necesita que los pacientes y el personal del consultorio puedan consultar **qué horarios están realmente libres** antes de reservar. El sistema debe calcular franjas disponibles considerando el horario laboral del profesional, los bloqueos (por profesional y por sillón/box) y los turnos ya existentes, respetando la duración de la prestación seleccionada.

Este change (**C-04**) implementa el endpoint `GET /api/availability` y el motor de cálculo de franjas libres. Es el puente entre la configuración del consultorio (`C-03`) y la gestión de turnos (`C-05`): sin él, el paciente no puede reservar de forma autónoma y la recepción no puede consultar huecos.

## 2. Motivación y Problema a Resolver

Sin un motor de disponibilidad:
- El paciente no puede saber qué horarios quedan libres y debe llamar al consultorio.
- La recepción debe revisar la agenda manualmente para ofrecer alternativas.
- El cálculo de huecos libres debe replicarse en cada pantalla (mala consistencia).
- No hay una fuente única de verdad para `C-05` al momento de validar reservas.

## 3. Alcance Propuesto (In-Scope)

- **Endpoint público** `GET /api/availability` con parámetros: `profesionalId`, `fecha`, `prestacionId` y `sillonBoxId` (opcional).
- **Motor de cálculo** de franjas libres (`AvailabilityCalculator`) que:
  - Considera los `HorarioAtencion` vigentes del profesional para el día de la semana de la fecha.
  - Excluye franjas afectadas por `Bloqueo` de profesional y/o sillón/box.
  - Excluye franjas ocupadas por `Turno` existentes (excluyendo estados `CANCELADO`).
  - Calcula el bloque completo como `duracion_minutos` de la prestación seleccionada.
  - Permite turnos consecutivos borde a borde (fin = inicio del siguiente).
  - Devuelve la lista de horarios de inicio disponibles.
- **DTOs de respuesta** con inicio, fin y metadatos básicos de cada franja.
- **Tests**: unitarios del calculador (sin base) e integración del endpoint sobre H2.

## 4. Fuera de Alcance (Non-Goals)

- Crear, reprogramar o cancelar turnos (reservado para `C-05`).
- CRUD de profesionales, sillones, horarios, bloqueos y prestaciones (reservado para `C-03`).
- Autenticación de la consulta de disponibilidad (el endpoint es público según `C-01`).
- Persistencia de la disponibilidad (se calcula en memoria a partir de los datos existentes).
- Exclusiones globales del MVP: WhatsApp, Mercado Pago, AFIP/ARCA, odontograma, multi-sucursal.

## 5. Criterios de Éxito y Verificación

1. `./gradlew test` en `backend/` compila en verde sin requerir PostgreSQL ni Docker.
2. `GET /api/availability` responde `200 OK` con una lista de franjas libres o `[]` si no hay disponibilidad.
3. Los escenarios del spec (feliz, sin horario, borde a borde, bloqueo por sillón, duración no ajustable) tienen tests automatizados en verde.
4. La lógica de cálculo es reutilizable por `C-05` al validar reservas.

## 6. Dependencias y Riesgos

- **Dependencias reales**: entidades `Profesional`, `SillonBox`, `Prestacion`, `HorarioAtencion`, `Bloqueo` (creadas en `C-03`) y `Turno` (creada en `C-05`).
- **Estrategia recomendada**: implementar primero el calculador puro con DTOs y tests unitarios; conectar los repositorios cuando las entidades existan en `main`.
- **Riesgo**: desincronización con la lógica anti-solapamiento de `C-05` si se definen reglas distintas.
- **Pregunta abierta IN-05**: granularidad de la grilla (15 o 30 minutos). Se propone 15 minutos como valor por defecto configurable.

## 7. Aprobación del propose (registro manual)

- **Fecha**: 2026-11-23.
- **Estado**: aprobado. Alcance confirmado sin cambios de fondo: `GET /api/availability`, calculador puro, DTOs y tests unitarios + integración.
- **Decisiones incorporadas**: grilla de 15 minutos configurable (`availability.grid-minutes`, A4); entidades C-03 en `com.consultorio.admin.*` en modo solo lectura (A2); `Turno` desacoplado mediante `TurnoDisponibilidadPort` para C-05 (A3).
- **Verificación**: `openspec validate c-04-disponibilidad-turnos` → *valid* (delta spec en `specs/c-04-disponibilidad-turnos/spec.md`).
