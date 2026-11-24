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

class AnulacionControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final AnulacionService anulacionService = mock(AnulacionService.class);
    private final AnulacionController controller = new AnulacionController(anulacionService);
    @Test void anularCompromisoDelegaAlServicio() {
        AnularCompromisoRequest datos = mock(AnularCompromisoRequest.class);
        AnulacionResponse esperado = mock(AnulacionResponse.class); when(anulacionService.anularCompromiso(datos)).thenReturn(esperado); assertSame(esperado, controller.anularCompromiso(datos)); verify(anulacionService).anularCompromiso(datos);
    }
    @Test void obtenerAnulacionDelegaAlServicio() {
        String folio = "prueba";
        Long ejecutivoId = 1L;
        Long usuarioId = 1L;
        AnulacionResponse esperado = mock(AnulacionResponse.class); when(anulacionService.obtenerAnulacion(ejecutivoId, usuarioId, folio)).thenReturn(esperado); assertSame(esperado, controller.obtenerAnulacion(folio, ejecutivoId, usuarioId)); verify(anulacionService).obtenerAnulacion(ejecutivoId, usuarioId, folio);
    }
}
