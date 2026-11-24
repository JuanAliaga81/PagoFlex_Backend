package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.*;
import cl.duoc.pagoflex.cobranza_service.model.*;
import cl.duoc.pagoflex.cobranza_service.repo.*;
import cl.duoc.pagoflex.cobranza_service.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class RevisionCompromisoControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final RevisionCompromisoService servicio = mock(RevisionCompromisoService.class);
    private final String claveInterna = "clave-prueba";
    private final RevisionCompromisoController controller = new RevisionCompromisoController(servicio, claveInterna);
    @Test void rechazaClaveAbrir() {
        error(403, () -> controller.abrir("incorrecta", null)); verifyNoInteractions(servicio);
    }
    @Test void rechazaClaveResolver() {
        error(403, () -> controller.resolver("incorrecta", null)); verifyNoInteractions(servicio);
    }
    @Test void abrirConvierteRespuesta() {
        var datos = new RevisionCompromisoRequest(1L,"F1"); var c = new Compromiso(); c.setEstado(EstadoCompromiso.EN_REVISION); when(servicio.abrir(datos)).thenReturn(c); assertEquals("EN_REVISION",controller.abrir(claveInterna,datos).estado());
    }
    @Test void resolverConvierteRespuesta() {
        var datos = new RevisionCompromisoRequest(1L,"F1"); var c = new Compromiso(); c.setEstado(EstadoCompromiso.PENDIENTE); when(servicio.resolver(datos)).thenReturn(c); assertEquals("PENDIENTE",controller.resolver(claveInterna,datos).estado());
    }
}
