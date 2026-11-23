# Design — C-04 Disponibilidad de Turnos

## 1. Decisiones de Diseño

### 1.1 Separación entre cálculo puro y adaptadores de persistencia

- **`AvailabilityCalculator`** (dominio puro, sin Spring/JPA): recibe DTOs inmutables (`WorkWindow`, `Block`, `Appointment`, `durationMinutes`, `gridMinutes`) y devuelve `List<AvailabilitySlot>`.
  - Ventaja: testeable sin base de datos, sin mocks de repositorios, rápido (< 50ms por test unitario).
  - Reutilizable por `C-05` para validar que una reserva propuesta encaje.
- **`AvailabilityService`** (capa de aplicación): consulta los repositorios, arma los DTOs y llama al calculador.
- **`AvailabilityController`** (capa API): valida parámetros, invoca el servicio y serializa la respuesta.

### 1.2 Estructura de paquetes

Se mantiene la estructura por capas ya usada en `C-01` y `C-02` para evitar colisiones:

```
com.consultorio
├── api.controller.AvailabilityController
├── application.dto.{AvailabilityRequest, AvailabilitySlotResponse, ...}
├── application.service.AvailabilityService
├── domain.model.{HorarioAtencion, Bloqueo, Turno, Profesional, SillonBox, Prestacion}
├── domain.repository.{HorarioAtencionRepository, BloqueoRepository, TurnoRepository, ...}
└── domain.availability.{AvailabilityCalculator, WorkWindow, Block, Appointment, AvailabilitySlot}
```

El paquete `domain.availability` contiene la lógica pura y no depende de JPA.

### 1.3 Algoritmo de cálculo de franjas

1. Determinar el `dia_semana` (1–7) de la fecha recibida.
2. Obtener las ventanas de trabajo (`hora_inicio` → `hora_fin`) del profesional para ese día.
3. Para cada ventana:
   - Generar candidatas cada `gridMinutes` (default 15) desde `hora_inicio` hasta `hora_fin - durationMinutes`.
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

## 3. Preguntas de Diseño a Resolver con el Equipo

1. ¿Quién define las entidades `HorarioAtencion`, `Bloqueo` y `Turno`? (C-03 y C-05). Se propone crearlas en `C-04` como entidades de solo lectura si no existen, y unificar después.
2. Granularidad de la grilla: 15 o 30 minutos (IN-05). Se propone 15 configurable por variable de entorno.
3. ¿La disponibilidad se calcula para un sillón específico o para cualquier sillón libre? El alcance pide "por profesional y sillón/box"; se propone filtrar por sillón si se pasa el parámetro.
