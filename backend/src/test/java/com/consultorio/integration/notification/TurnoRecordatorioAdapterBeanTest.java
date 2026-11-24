package com.consultorio.integration.notification;

import com.consultorio.notification.application.port.TurnoParaRecordarPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica que el bean real reemplaza al no-op de C-06 sin modificar ese módulo. */
@SpringBootTest
class TurnoRecordatorioAdapterBeanTest {

    @Autowired
    private TurnoParaRecordarPort port;

    @Test
    void elPuertoDeRecordatoriosEsLaImplementacionReal() {
        assertTrue(port instanceof TurnoRecordatorioAdapter,
            "El bean no-op ya no debe estar activo");
    }
}
