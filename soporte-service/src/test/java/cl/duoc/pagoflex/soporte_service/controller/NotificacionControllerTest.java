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

class NotificacionControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final NotificacionService servicio = mock(NotificacionService.class);
    private final String claveInterna = "clave-prueba";
    private final NotificacionController controller = new NotificacionController(servicio, claveInterna);
    @Test void listarDelegaAlServicio() {
        Long usuarioId = 1L;
        List<NotificacionResponse> esperado = List.of(); when(servicio.listar(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.listar(usuarioId)); verify(servicio).listar(usuarioId);
    }
    @Test void leerDelegaAlServicio() {
        Long id = 1L;
        Long usuarioId = 1L;
        NotificacionResponse esperado = mock(NotificacionResponse.class); when(servicio.marcarLeida(usuarioId, id)).thenReturn(esperado); assertSame(esperado, controller.leer(id, usuarioId)); verify(servicio).marcarLeida(usuarioId, id);
    }
    @Test void cuentaNoLeidas() {
        when(servicio.noLeidas(1L)).thenReturn(3L); assertEquals(3L,controller.noLeidas(1L).get("cantidad"));
    }
    @Test void marcaTodas() {
        when(servicio.marcarTodas(1L)).thenReturn(2); assertEquals(2,controller.leerTodas(1L).get("cantidad"));
    }
    @Test void rechazaClave() {
        error(403, () -> controller.crearEvento("incorrecta",null)); verifyNoInteractions(servicio);
    }
    @Test void creaEvento() {
        var datos = mock(CrearNotificacionRequest.class); var esperado = mock(NotificacionResponse.class); when(servicio.crear(datos)).thenReturn(esperado); assertSame(esperado, controller.crearEvento(claveInterna,datos));
    }
}
