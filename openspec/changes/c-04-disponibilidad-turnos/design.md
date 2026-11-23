# Design — C-04 Disponibilidad de Turnos

## 1. Decisiones de Diseño

### 1.1 Separación entre cálculo puro y adaptadores de persistencia

- **`AvailabilityCalculator`** (dominio puro, sin Spring/JPA): recibe DTOs inmutables (`WorkWindow`, `Block`, `Appointment`, `durationMinutes`, `gridMinutes`) y devuelve `List<AvailabilitySlot>`.
  - Ventaja: testeable sin base de datos, sin mocks de repositorios, rápido (< 50ms por test unitario).
  - Reutilizable por `C-05` para validar que una reserva propuesta encaje.
- **`AvailabilityService`** (capa de aplicación): consulta los repositorios, arma los DTOs y llama al calculador.
- **`AvailabilityController`** (capa API): valida parámetros, invoca el servicio y serializa la respuesta.

### 1.2 Estructura de paquetes

Se mantiene la estructura por capas ya usada en `C-01` y `C-02` para evitar colisiones. Las entidades de configuración se **lean** del paquete real de `C-03` (`com.consultorio.admin.*`); no se duplican:

```
com.consultorio
├── api.controller.AvailabilityController
├── application.dto.{AvailabilityRequest, AvailabilitySlotResponse, AvailabilityResponse}
├── application.service.AvailabilityService
├── admin.domain.model.{Profesional, SillonBox, Prestacion, HorarioAtencion, Bloqueo}   (C-03, solo lectura)
├── admin.domain.repository.{ProfesionalRepository, SillonBoxRepository, PrestacionRepository,
│                            HorarioAtencionRepository, BloqueoRepository}
├── domain.availability.{AvailabilityCalculator, WorkWindow, Block, Appointment, AvailabilitySlot,
│                        TurnoDisponibilidadPort}   (puerto de solo lectura; C-05 lo implementa)
└── infrastructure.config.AvailabilityProperties   (gridMinutes, default 15)
```

El paquete `domain.availability` contiene la lógica pura y no depende de JPA. `TurnoDisponibilidadPort` desacopla C-04 de la entidad `Turno` de C-05 (mismo patrón que `TurnoParaRecordarPort` de C-06).

### 1.3 Algoritmo de cálculo de franjas

1. Determinar el `dia_semana` (1–7) de la fecha recibida.
2. Obtener las ventanas de trabajo (`hora_inicio` → `hora_fin`) del profesional para ese día.
3. Para cada ventana:
   - Generar candidatas cada `gridMinutes` (default 15) desde `hora_inicio` hasta `hora_fin - durationMinutes`. El paso efectivo es `max(gridMinutes, durationMinutes)` para no ofrecer inicios que se solapen entre sí: con una prestación de 30 min se obtienen 09:00, 09:30, 10:00, ... (Esc. 1 del spec), aunque la grilla esté configurada en 15 min. Ajuste registrado durante C3.
   - Descartar toda candidata cuyo `[inicio, inicio + duration)`:
     - Se solape con un `Bloqueo` del profesional o del sillón/box indicado.
     - Se solape con un `Turno` existente (excluyendo `CANCELADO`) del profesional o del sillón/box indicado.
   - Conservar las candidatas que queden.
4. Ordenar por hora de inicio y devolver.

**Solapamiento estricto vs. borde a borde**: dos intervalos se solapan si `inicioA < finB && inicioB < finA`. Si `finA == inicioB`, **no** se solapan (borde permitido).

### 1.4 Parámetros del endpoint

| Parámetro | Tipo | Obligatorio | Notas |
|-----------|------|-------------|-------|
| `profesionalId` | UUID | Sí | Profesional activo |
| `fecha` | `YYYY-MM-DD` | Sí | Fecha de consulta |
| `prestacionId` | UUID | Sí | Prestación cuya duración define el bloque |
| `sillonBoxId` | UUID | No | Si se indica, se filtra la disponibilidad de ese sillón/box |

Respuesta `200 OK`:
```json
{
  "profesionalId": "...",
  "fecha": "2026-11-25",
  "duracionMinutos": 30,
  "franjas": [
    { "inicio": "2026-11-25T09:00:00", "fin": "2026-11-25T09:30:00" },
    { "inicio": "2026-11-25T09:30:00", "fin": "2026-11-25T10:00:00" }
  ]
}
```

Si no hay disponibilidad, `franjas: []` (no es error).

### 1.5 Manejo de errores

- Parámetros inválidos (fecha mal formada, IDs faltantes) → `400` con `fieldErrors`.
- Profesional/prestación inexistente → `404` (`ResourceNotFoundException`).
- No hay horario de atención ese día → `200` con lista vacía.

## 2. Estrategia de Testing

- **Unitario** (`AvailabilityCalculatorTest`): sin Spring, sin BD. Cubre todos los escenarios del spec.
- **Integración** (`AvailabilityControllerTest` / `AvailabilityIntegrationTest`): `@WebMvcTest` o `@SpringBootTest` sobre H2, con datos sembrados en `@BeforeEach` (datos ficticios, RN-EX-01).
- **Rendimiento objetivo**: unit < 50ms, slice < 100ms.

## 3. Decisiones Registradas

1. **Entidades (A2/A3):** `HorarioAtencion` y `Bloqueo` se leen de `C-03` (`com.consultorio.admin.domain.model`); no se crean copias. `Turno` aún no existe (C-05): C-04 define el puerto de solo lectura `TurnoDisponibilidadPort` y el DTO inmutable `Appointment`; C-05 lo implementa contra su entidad real. Evita colisiones de mapeo JPA.
2. **Granularidad de la grilla (A4, IN-05):** 15 minutos por defecto, configurable mediante la propiedad `availability.grid-minutes` (variable de entorno `AVAILABILITY_GRID_MINUTES`). Decisión registrada en la tarea A4.
3. **Sillón/box (alcance):** se filtra por sillón cuando se pasa el parámetro opcional `sillonBoxId`; si no se pasa, la disponibilidad se calcula para el profesional. Los bloqueos con profesional y/o sillón se aplican con semántica OR.
