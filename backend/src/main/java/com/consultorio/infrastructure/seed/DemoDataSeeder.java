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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    private static final int HORARIO_COMPLETO = 0;
    private static final int HORARIO_MANANA = 1;
    private static final int HORARIO_TARDE = 2;
    private static final int HORARIO_MANANA_MAR_JUE = 3;

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

        List<Profesional> profesionales = new ArrayList<>();
        profesionales.add(crearProfesional("Dra. Ana Demo", "MP-99999", HORARIO_COMPLETO));
        profesionales.add(crearProfesional("Dr. Martín Gómez", "MP-10001", HORARIO_MANANA));
        profesionales.add(crearProfesional("Dra. Lucía Fernández", "MP-10002", HORARIO_TARDE));
        profesionales.add(crearProfesional("Dr. Sebastián Ríos", "MP-10003", HORARIO_COMPLETO));
        profesionales.add(crearProfesional("Dra. Camila Ortiz", "MP-10004", HORARIO_MANANA_MAR_JUE));
        profesionales.add(crearProfesional("Dr. Diego Vega", "MP-10005", HORARIO_TARDE));
        profesionales.add(crearProfesional("Dra. Florencia Acosta", "MP-10006", HORARIO_COMPLETO));
        profesionales.add(crearProfesional("Dr. Nicolás Barrios", "MP-10007", HORARIO_MANANA));
        profesionales.add(crearProfesional("Dra. Valentina Costa", "MP-10008", HORARIO_TARDE));
        profesionales.add(crearProfesional("Dr. Agustín Duarte", "MP-10009", HORARIO_COMPLETO));
        profesionales.add(crearProfesional("Dra. Jazmín Escobar", "MP-10010", HORARIO_MANANA_MAR_JUE));
        profesionales.add(crearProfesional("Dr. Tomás Farías", "MP-10011", HORARIO_TARDE));
        profesionales.add(crearProfesional("Dra. Elena Gómez", "MP-10012", HORARIO_COMPLETO));
        profesionales.add(crearProfesional("Dr. Bruno Ibáñez", "MP-10013", HORARIO_MANANA));
        profesionales.add(crearProfesional("Dra. Sofía Juárez", "MP-10014", HORARIO_TARDE));

        SillonBox sillon = new SillonBox();
        sillon.setNombre("Sillón 1");
        sillonBoxRepository.save(sillon);

        SillonBox sillon2 = new SillonBox();
        sillon2.setNombre("Sillón 2");
        sillonBoxRepository.save(sillon2);

        Prestacion consulta = crearPrestacion("Consulta general", 30, "Control y diagnóstico inicial");
        Prestacion limpieza = crearPrestacion("Limpieza dental", 45, "Higiene profesional");
        Prestacion revision = crearPrestacion("Revisión completa", 60, "Evaluación integral");

        LocalDate diaDemo = diaHabil(1);
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setSillonBox(sillon);
        bloqueo.setFechaHoraInicio(diaDemo.atTime(10, 0));
        bloqueo.setFechaHoraFin(diaDemo.atTime(10, 30));
        bloqueo.setMotivo("Mantenimiento del sillón (demo)");
        bloqueoRepository.save(bloqueo);

        List<Paciente> pacientes = new ArrayList<>();
        pacientes.add(crearPaciente("Juan Pérez (paciente ficticio)", "99999999", "paciente.demo@gmail.com"));
        pacientes.add(crearPaciente("Camila Pereyra", "38901234", "camila.pereyra.test@gmail.com"));
        pacientes.add(crearPaciente("Lucas Sosa", "40123789", "lucas.sosa.test@gmail.com"));
        pacientes.add(crearPaciente("Valentina Quiroga", "35678123", "valentina.quiroga.test@gmail.com"));
        pacientes.add(crearPaciente("Federico Moya", "41234567", "federico.moya.test@gmail.com"));
        pacientes.add(crearPaciente("Romina Duarte", "30555123", null));
        pacientes.add(crearPaciente("Nicolás Aguirre", "37654321", "nicolas.aguirre.test@gmail.com"));
        pacientes.add(crearPaciente("Lucía Barsotti", "33445678", "lucia.barsotti.test@gmail.com"));
        pacientes.add(crearPaciente("Martín Cáceres", "40222333", "martin.caceres.test@gmail.com"));
        pacientes.add(crearPaciente("Sofía Delgado", "36111222", "sofia.delgado.test@gmail.com"));
        pacientes.add(crearPaciente("Agustín Espinoza", "39333444", "agustin.espinoza.test@gmail.com"));
        pacientes.add(crearPaciente("Jazmín Flores", "34555666", "jazmin.flores.test@gmail.com"));
        pacientes.add(crearPaciente("Tomás Giménez", "40888777", "tomas.gimenez.test@gmail.com"));
        pacientes.add(crearPaciente("Elena López", "31222999", "elena.lopez.test@gmail.com"));
        pacientes.add(crearPaciente("Bruno Martínez", "39777111", "bruno.martinez.test@gmail.com"));

        // 15 turnos ficticios en 4 estados (4 RESERVADO, 4 CONFIRMADO, 4 COMPLETADO, 3 CANCELADO).
        // Se reparten entre días pasados y futuros sin solaparse en el Sillón 1 (el Sillón 2 queda libre).
        crearTurno(sillon, pacientes.get(0), profesionales.get(0), consulta, diaHabil(-3), LocalTime.of(9, 0), EstadoTurno.COMPLETADO);
        crearTurno(sillon, pacientes.get(4), profesionales.get(3), revision, diaHabil(-3), LocalTime.of(10, 0), EstadoTurno.COMPLETADO);
        crearTurno(sillon, pacientes.get(1), profesionales.get(1), limpieza, diaHabil(-3), LocalTime.of(11, 0), EstadoTurno.COMPLETADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(2), revision, diaHabil(-2), LocalTime.of(14, 0), EstadoTurno.COMPLETADO);
        crearTurno(sillon, pacientes.get(2), profesionales.get(5), consulta, diaHabil(-2), LocalTime.of(15, 0), EstadoTurno.CANCELADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(3), limpieza, diaHabil(-1), LocalTime.of(9, 0), EstadoTurno.CANCELADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(0), consulta, diaDemo, LocalTime.of(9, 30), EstadoTurno.RESERVADO);
        crearTurno(sillon, pacientes.get(4), profesionales.get(3), revision, diaHabil(2), LocalTime.of(9, 0), EstadoTurno.RESERVADO);
        crearTurno(sillon, pacientes.get(5), profesionales.get(0), consulta, diaHabil(2), LocalTime.of(10, 0), EstadoTurno.CANCELADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(2), revision, diaHabil(2), LocalTime.of(14, 0), EstadoTurno.CONFIRMADO);
        crearTurno(sillon, pacientes.get(1), profesionales.get(3), limpieza, diaHabil(3), LocalTime.of(10, 30), EstadoTurno.CONFIRMADO);
        crearTurno(sillon, pacientes.get(2), profesionales.get(1), limpieza, diaHabil(3), LocalTime.of(11, 15), EstadoTurno.RESERVADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(5), revision, diaHabil(3), LocalTime.of(14, 0), EstadoTurno.RESERVADO);
        crearTurno(sillon, pacientes.get(0), profesionales.get(0), consulta, diaHabil(4), LocalTime.of(9, 0), EstadoTurno.CONFIRMADO);
        crearTurno(sillon, pacientes.get(3), profesionales.get(3), revision, diaHabil(4), LocalTime.of(11, 0), EstadoTurno.CONFIRMADO);

        log.info("[DEMO] Datos ficticios cargados. Login: DNI {} / contraseña {}.", DNI_DEMO, PASSWORD_DEMO);
        log.info("[DEMO] 15 pacientes, 15 profesionales y 15 turnos ficticios (4 RESERVADO, 4 CONFIRMADO, 4 COMPLETADO, 3 CANCELADO).");
        log.info("[DEMO] 2 sillones: {} y {}, prestaciones: 3, turno demo: {} 09:30-10:00, bloqueo: {} 10:00-10:30.",
                sillon.getNombre(), sillon2.getNombre(), diaDemo, diaDemo);
    }

    private Profesional crearProfesional(String nombreCompleto, String matricula, int tipoHorario) {
        Profesional profesional = new Profesional();
        profesional.setNombreCompleto(nombreCompleto);
        profesional.setMatricula(matricula);
        Profesional guardado = profesionalRepository.save(profesional);
        switch (tipoHorario) {
            case HORARIO_COMPLETO -> {
                crearHorarios(guardado, DIAS_LUN_VIE, LocalTime.of(9, 0), LocalTime.of(12, 0));
                crearHorarios(guardado, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));
            }
            case HORARIO_MANANA -> crearHorarios(guardado, DIAS_LUN_VIE, LocalTime.of(9, 0), LocalTime.of(12, 0));
            case HORARIO_TARDE -> crearHorarios(guardado, DIAS_LUN_VIE, LocalTime.of(14, 0), LocalTime.of(17, 0));
            case HORARIO_MANANA_MAR_JUE -> crearHorarios(guardado, DIAS_MAR_JUE, LocalTime.of(9, 0), LocalTime.of(12, 0));
            default -> throw new IllegalArgumentException("Tipo de horario desconocido: " + tipoHorario);
        }
        return guardado;
    }

    private Paciente crearPaciente(String nombreCompleto, String dni, String emailRecuperacion) {
        Paciente paciente = new Paciente();
        paciente.setNombreCompleto(nombreCompleto);
        paciente.setDni(dni);
        paciente.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
        paciente.setEmailRecuperacion(emailRecuperacion);
        return pacienteRepository.save(paciente);
    }

    private void crearTurno(SillonBox sillon, Paciente paciente, Profesional profesional, Prestacion prestacion,
                            LocalDate fecha, LocalTime inicio, EstadoTurno estado) {
        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setSillonBox(sillon);
        turno.setPrestacion(prestacion);
        turno.setFechaHoraInicio(fecha.atTime(inicio));
        turno.setFechaHoraFin(fecha.atTime(inicio).plusMinutes(prestacion.getDuracionMinutos()));
        turno.setEstado(estado);
        turno.setCreadoPor(paciente.getDni());
        turnoRepository.save(turno);
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

    /** Día hábil desplazado {@code offset} días desde hoy (negativo = pasado). */
    private LocalDate diaHabil(int offset) {
        LocalDate fecha = LocalDate.now();
        int pasos = 0;
        while (pasos < Math.abs(offset)) {
            fecha = fecha.plusDays(offset > 0 ? 1 : -1);
            if (fecha.getDayOfWeek().getValue() <= 5) {
                pasos++;
            }
        }
        return fecha;
    }
}
