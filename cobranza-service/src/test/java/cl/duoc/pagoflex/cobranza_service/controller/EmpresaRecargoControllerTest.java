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

class EmpresaRecargoControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final VencimientoService vencimientoService = mock(VencimientoService.class);
    private final EmpresaRecargoController controller = new EmpresaRecargoController(vencimientoService);
    @Test void obtenerRecargoDelegaAlServicio() {
        Long id = 1L;
        EmpresaRecargoResponse esperado = mock(EmpresaRecargoResponse.class); when(vencimientoService.obtenerRecargoEmpresa(id)).thenReturn(esperado); assertSame(esperado, controller.obtenerRecargo(id)); verify(vencimientoService).obtenerRecargoEmpresa(id);
    }
    @Test void configurarRecargoDelegaAlServicio() {
        Long id = 1L;
        ConfigurarRecargoRequest datos = mock(ConfigurarRecargoRequest.class);
        EmpresaRecargoResponse esperado = mock(EmpresaRecargoResponse.class); when(vencimientoService.configurarRecargo(id, datos)).thenReturn(esperado); assertSame(esperado, controller.configurarRecargo(id, datos)); verify(vencimientoService).configurarRecargo(id, datos);
    }
}
