package cl.duoc.pagoflex.soporte_service.controller;

import cl.duoc.pagoflex.soporte_service.dto.*;
import cl.duoc.pagoflex.soporte_service.model.*;
import cl.duoc.pagoflex.soporte_service.repository.*;
import cl.duoc.pagoflex.soporte_service.service.*;
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

class ReporteControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final ReporteService servicio = mock(ReporteService.class);
    private final ReporteController controller = new ReporteController(servicio);
    @Test void crearDelegaAlServicio() {
        CrearReporteRequest datos = mock(CrearReporteRequest.class);
        ReporteResponse esperado = mock(ReporteResponse.class); when(servicio.crear(datos)).thenReturn(esperado); assertSame(esperado, controller.crear(datos)); verify(servicio).crear(datos);
    }
    @Test void listarDelegaAlServicio() {
        Long usuarioId = 1L;
        List<ReporteResponse> esperado = List.of(); when(servicio.listarUsuario(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.listar(usuarioId)); verify(servicio).listarUsuario(usuarioId);
    }
    @Test void obtenerDelegaAlServicio() {
        String codigo = "prueba";
        Long usuarioId = 1L;
        ReporteResponse esperado = mock(ReporteResponse.class); when(servicio.obtenerUsuario(usuarioId, codigo)).thenReturn(esperado); assertSame(esperado, controller.obtener(codigo, usuarioId)); verify(servicio).obtenerUsuario(usuarioId, codigo);
    }
    @Test void gestionDelegaAlServicio() {
        Long agenteId = 1L;
        EstadoReporte estado = EstadoReporte.PENDIENTE;
        List<ReporteResponse> esperado = List.of(); when(servicio.listarGestion(agenteId, estado)).thenReturn(esperado); assertSame(esperado, controller.gestion(agenteId, estado)); verify(servicio).listarGestion(agenteId, estado);
    }
    @Test void tomarDelegaAlServicio() {
        Long id = 1L;
        TomarReporteRequest datos = mock(TomarReporteRequest.class);
        ReporteResponse esperado = mock(ReporteResponse.class); when(servicio.tomar(id, datos)).thenReturn(esperado); assertSame(esperado, controller.tomar(id, datos)); verify(servicio).tomar(id, datos);
    }
    @Test void resolverDelegaAlServicio() {
        Long id = 1L;
        ResolverReporteRequest datos = mock(ResolverReporteRequest.class);
        ReporteResponse esperado = mock(ReporteResponse.class); when(servicio.resolver(id, datos)).thenReturn(esperado); assertSame(esperado, controller.resolver(id, datos)); verify(servicio).resolver(id, datos);
    }
}
