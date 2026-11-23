package com.consultorio.domain.repository;

import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Consultas de agenda. La regla estricta {@code inicioA < finB && inicioB < finA}
 * permite turnos consecutivos borde a borde; los cancelados nunca ocupan horario.
 */
public interface TurnoRepository extends JpaRepository<Turno, UUID> {

    @Query("""
        select t from Turno t
        where t.activo = true
          and t.estado <> com.consultorio.domain.model.EstadoTurno.CANCELADO
          and (:turnoId is null or t.id <> :turnoId)
          and t.profesional.id = :profesionalId
          and t.fechaHoraInicio < :fin
          and t.fechaHoraFin > :inicio
        """)
    List<Turno> findSolapesPorProfesional(@Param("profesionalId") UUID profesionalId,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin,
                                          @Param("turnoId") UUID turnoId);

    @Query("""
        select t from Turno t
        where t.activo = true
          and t.estado <> com.consultorio.domain.model.EstadoTurno.CANCELADO
          and (:turnoId is null or t.id <> :turnoId)
          and t.sillonBox.id = :sillonBoxId
          and t.fechaHoraInicio < :fin
          and t.fechaHoraFin > :inicio
        """)
    List<Turno> findSolapesPorSillon(@Param("sillonBoxId") UUID sillonBoxId,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin,
                                     @Param("turnoId") UUID turnoId);

    @Query("""
        select t from Turno t
        where (:profesionalId is null or t.profesional.id = :profesionalId)
          and (:desde is null or t.fechaHoraInicio >= :desde)
          and (:hasta is null or t.fechaHoraInicio <= :hasta)
          and (:estado is null or t.estado = :estado)
        order by t.fechaHoraInicio
        """)
    List<Turno> buscarConFiltros(@Param("profesionalId") UUID profesionalId,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta,
                                 @Param("estado") EstadoTurno estado);

    /** Fuente de datos del puerto de disponibilidad de C-04; incluye cancelados para que se ignoren en el cálculo. */
    @Query("""
        select t from Turno t
        where t.activo = true
          and t.profesional.id = :profesionalId
          and t.fechaHoraInicio >= :desde
          and t.fechaHoraInicio <= :hasta
          and (:sillonBoxId is null or t.sillonBox.id = :sillonBoxId)
        order by t.fechaHoraInicio
        """)
    List<Turno> buscarOcupados(@Param("profesionalId") UUID profesionalId,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta,
                               @Param("sillonBoxId") UUID sillonBoxId);

    List<Turno> findByProfesionalIdAndFechaHoraInicioBetween(UUID profesionalId,
                                                             LocalDateTime desde,
                                                             LocalDateTime hasta);

    List<Turno> findBySillonBoxIdAndFechaHoraInicioBetween(UUID sillonBoxId,
                                                           LocalDateTime desde,
                                                           LocalDateTime hasta);
}
