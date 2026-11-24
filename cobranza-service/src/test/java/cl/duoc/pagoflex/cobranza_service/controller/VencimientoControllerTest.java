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

class VencimientoControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final VencimientoService vencimientoService = mock(VencimientoService.class);
    private final VencimientoController controller = new VencimientoController(vencimientoService);
    @Test void detectarVencidosDelegaAlServicio() {
        Long usuarioId = 1L;
        List<VencimientoDetectadoResponse> esperado = List.of(); when(vencimientoService.detectarVencidos(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.detectarVencidos(usuarioId)); verify(vencimientoService).detectarVencidos(usuarioId);
    }
    @Test void actualizarVencimientosDelegaAlServicio() {
        Long usuarioId = 1L;
        ResultadoActualizacionVencimientosResponse esperado = mock(ResultadoActualizacionVencimientosResponse.class); when(vencimientoService.actualizarVencimientos(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.actualizarVencimientos(usuarioId)); verify(vencimientoService).actualizarVencimientos(usuarioId);
    }
}
