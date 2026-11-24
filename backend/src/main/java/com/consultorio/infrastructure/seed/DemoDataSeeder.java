package com.consultorio.infrastructure.seed;

import com.consultorio.admin.domain.model.Bloqueo;
import com.consultorio.admin.domain.model.HorarioAtencion;
import com.consultorio.admin.domain.model.Prestacion;
import com.consultorio.admin.domain.model.Profesional;
import com.consultorio.admin.domain.model.SillonBox;
import com.consultorio.admin.domain.repository.BloqueoRepository;
import com.consultorio.admin.domain.repository.HorarioAtencionRepository;
import com.consultorio.admin.domain.repository.PrestacionRepository;
import com.consultorio.admin.domain.repository.ProfesionalRepository;
import com.consultorio.admin.domain.repository.SillonBoxRepository;
import com.consultorio.domain.model.EstadoTurno;
import com.consultorio.domain.model.Paciente;
import com.consultorio.domain.model.Turno;
import com.consultorio.domain.repository.PacienteRepository;
import com.consultorio.domain.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Carga de datos FICTICIOS para probar el frontend en local (Ley 25.326).
 * Solo se activa con el perfil {@code demo}: {@code --spring.profiles.active=demo}.
 * Es idempotente: si ya hay profesionales, no vuelve a insertar nada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final String DNI_DEMO = "99999999";
    private static final String PASSWORD_DEMO = "demo1234";

    private final ProfesionalRepository profesionalRepository;
    private final SillonBoxRepository sillonBoxRepository;
    private final PrestacionRepository prestacionRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final BloqueoRepository bloqueoRepository;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (profesionalRepository.count() > 0) {
            log.info("[DEMO] Ya existen datos cargados; se omite el seed.");
            return;
        }

        Profesional profesional = new Profesional();
        profesional.setNombreCompleto("Dra. Ana Demo");
        profesional.setMatricula("MP-99999");
        profesionalRepository.save(profesional);

        SillonBox sillon = new SillonBox();
        sillon.setNombre("Sillón 1");
        sillonBoxRepository.save(sillon);

        Prestacion consulta = crearPrestacion("Consulta general", 30, "Control y diagnóstico inicial");
        crearPrestacion("Limpieza dental", 45, "Higiene profesional");
        crearPrestacion("Revisión completa", 60, "Evaluación integral");

        for (int dia = 1; dia <= 5; dia++) {
            crearHorario(profesional, dia, LocalTime.of(9, 0), LocalTime.of(12, 0));
            crearHorario(profesional, dia, LocalTime.of(14, 0), LocalTime.of(17, 0));
        }

        LocalDate diaDemo = proximoDiaHabil();

        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setSillonBox(sillon);
        bloqueo.setFechaHoraInicio(diaDemo.atTime(10, 0));
        bloqueo.setFechaHoraFin(diaDemo.atTime(10, 30));
        bloqueo.setMotivo("Mantenimiento del sillón (demo)");
        bloqueoRepository.save(bloqueo);

        Paciente paciente = new Paciente();
        paciente.setNombreCompleto("Juan Pérez (paciente ficticio)");
        paciente.setDni(DNI_DEMO);
        paciente.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
        paciente.setEmailRecuperacion("paciente.demo@gmail.com");
        pacienteRepository.save(paciente);

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setSillonBox(sillon);
        turno.setPrestacion(consulta);
        turno.setFechaHoraInicio(diaDemo.atTime(9, 30));
        turno.setFechaHoraFin(diaDemo.atTime(10, 0));
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCreadoPor(DNI_DEMO);
        turnoRepository.save(turno);

        log.info("[DEMO] Datos ficticios cargados. Login: DNI {} / contraseña {}.", DNI_DEMO, PASSWORD_DEMO);
        log.info("[DEMO] Profesional: {}, sillón: {}, prestaciones: 3, turno ocupado: {} 09:30-10:00, bloqueo: {} 10:00-10:30.",
                profesional.getNombreCompleto(), sillon.getNombre(), diaDemo, diaDemo);
    }

    private Prestacion crearPrestacion(String nombre, int duracion, String descripcion) {
        Prestacion prestacion = new Prestacion();
        prestacion.setNombre(nombre);
        prestacion.setDuracionMinutos(duracion);
        prestacion.setDescripcion(descripcion);
        return prestacionRepository.save(prestacion);
    }

    private void crearHorario(Profesional profesional, int diaSemana, LocalTime inicio, LocalTime fin) {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horarioAtencionRepository.save(horario);
    }

    private LocalDate proximoDiaHabil() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        while (fecha.getDayOfWeek().getValue() > 5) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}
