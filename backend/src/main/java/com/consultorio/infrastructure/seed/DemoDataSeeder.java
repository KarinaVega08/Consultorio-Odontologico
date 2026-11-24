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
    private static final int[] DIAS_LUN_VIE = {1, 2, 3, 4, 5};
    private static final int[] DIAS_MAR_JUE = {2, 3, 4};

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

        Profesional ana = crearProfesional("Dra. Ana Demo", "MP-99999");
        crearHorarios(ana, DIAS_LUN_VIE, LocalTime.of(9, 0), LocalTime.of(12, 0));
        crearHorarios(ana, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));

        Profesional martin = crearProfesional("Dr. Martín Gómez", "MP-10001");
        crearHorarios(martin, DIAS_LUN_VIE, LocalTime.of(9, 0), LocalTime.of(12, 0));

        Profesional lucia = crearProfesional("Dra. Lucía Fernández", "MP-10002");
        crearHorarios(lucia, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));

        Profesional sebastian = crearProfesional("Dr. Sebastián Ríos", "MP-10003");
        crearHorarios(sebastian, DIAS_LUN_VIE, LocalTime.of(9, 0), LocalTime.of(12, 0));
        crearHorarios(sebastian, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));

        Profesional camila = crearProfesional("Dra. Camila Ortiz", "MP-10004");
        crearHorarios(camila, DIAS_MAR_JUE, LocalTime.of(9, 0), LocalTime.of(12, 0));

        Profesional diego = crearProfesional("Dr. Diego Vega", "MP-10005");
        crearHorarios(diego, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));

        SillonBox sillon = new SillonBox();
        sillon.setNombre("Sillón 1");
        sillonBoxRepository.save(sillon);

        Prestacion consulta = crearPrestacion("Consulta general", 30, "Control y diagnóstico inicial");
        crearPrestacion("Limpieza dental", 45, "Higiene profesional");
        crearPrestacion("Revisión completa", 60, "Evaluación integral");

        LocalDate diaDemo = proximoDiaHabil();

        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setSillonBox(sillon);
        bloqueo.setFechaHoraInicio(diaDemo.atTime(10, 0));
        bloqueo.setFechaHoraFin(diaDemo.atTime(10, 30));
        bloqueo.setMotivo("Mantenimiento del sillón (demo)");
        bloqueoRepository.save(bloqueo);

        Paciente juan = crearPaciente("Juan Pérez (paciente ficticio)", DNI_DEMO, "paciente.demo@gmail.com");
        crearPaciente("Camila Pereyra", "38901234", "camila.pereyra.test@gmail.com");
        crearPaciente("Lucas Sosa", "40123789", "lucas.sosa.test@gmail.com");
        crearPaciente("Valentina Quiroga", "35678123", "valentina.quiroga.test@gmail.com");
        crearPaciente("Federico Moya", "41234567", "federico.moya.test@gmail.com");
        crearPaciente("Romina Duarte", "30555123", null);

        Turno turno = new Turno();
        turno.setPaciente(juan);
        turno.setProfesional(ana);
        turno.setSillonBox(sillon);
        turno.setPrestacion(consulta);
        turno.setFechaHoraInicio(diaDemo.atTime(9, 30));
        turno.setFechaHoraFin(diaDemo.atTime(10, 0));
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCreadoPor(DNI_DEMO);
        turnoRepository.save(turno);

        log.info("[DEMO] Datos ficticios cargados. Login: DNI {} / contraseña {}.", DNI_DEMO, PASSWORD_DEMO);
        log.info("[DEMO] 6 pacientes y 6 profesionales ficticios, sillón: {}, prestaciones: 3, turno ocupado: {} 09:30-10:00, bloqueo: {} 10:00-10:30.",
                sillon.getNombre(), diaDemo, diaDemo);
    }

    private Profesional crearProfesional(String nombreCompleto, String matricula) {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto(nombreCompleto);
        profesional.setMatricula(matricula);
        return profesionalRepository.save(profesional);
    }

    private Paciente crearPaciente(String nombreCompleto, String dni, String emailRecuperacion) {
        Paciente paciente = new Paciente();
        paciente.setNombreCompleto(nombreCompleto);
        paciente.setDni(dni);
        paciente.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
        paciente.setEmailRecuperacion(emailRecuperacion);
        return pacienteRepository.save(paciente);
    }

    private void crearHorarios(Profesional profesional, int[] dias, LocalTime inicio, LocalTime fin) {
        for (int dia : dias) {
            crearHorario(profesional, dia, inicio, fin);
        }
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
