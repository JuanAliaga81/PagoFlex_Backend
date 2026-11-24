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

class PagoControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final PagoService pagoService = mock(PagoService.class);
    private final PagoController controller = new PagoController(pagoService);
    @Test void registrarPagoDelegaAlServicio() {
        RegistrarPagoRequest datos = mock(RegistrarPagoRequest.class);
        ComprobanteResponse esperado = mock(ComprobanteResponse.class); when(pagoService.registrarPago(datos)).thenReturn(esperado); assertSame(esperado, controller.registrarPago(datos)); verify(pagoService).registrarPago(datos);
    }
    @Test void obtenerHistorialDelegaAlServicio() {
        Long usuarioId = 1L;
        List<ComprobanteResponse> esperado = List.of(); when(pagoService.obtenerHistorial(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.obtenerHistorial(usuarioId)); verify(pagoService).obtenerHistorial(usuarioId);
    }
    @Test void obtenerComprobanteDelegaAlServicio() {
        String codigo = "prueba";
        Long usuarioId = 1L;
        ComprobanteResponse esperado = mock(ComprobanteResponse.class); when(pagoService.obtenerComprobante(usuarioId, codigo)).thenReturn(esperado); assertSame(esperado, controller.obtenerComprobante(codigo, usuarioId)); verify(pagoService).obtenerComprobante(usuarioId, codigo);
    }
}
