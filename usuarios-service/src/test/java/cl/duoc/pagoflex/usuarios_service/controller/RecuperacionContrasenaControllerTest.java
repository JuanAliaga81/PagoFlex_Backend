package cl.duoc.pagoflex.usuarios_service.controller;

import cl.duoc.pagoflex.usuarios_service.dto.*;
import cl.duoc.pagoflex.usuarios_service.model.*;
import cl.duoc.pagoflex.usuarios_service.repo.*;
import cl.duoc.pagoflex.usuarios_service.service.*;
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

class RecuperacionContrasenaControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final RecuperacionContrasenaService servicio = mock(RecuperacionContrasenaService.class);
    private final RecuperacionContrasenaController controller = new RecuperacionContrasenaController(servicio);
    @Test void solicitarDelegaAlServicio() {
        SolicitarRecuperacionRequest datos = mock(SolicitarRecuperacionRequest.class);
        controller.solicitar(datos); verify(servicio).solicitar(datos);
    }
    @Test void verificarDelegaAlServicio() {
        VerificarRecuperacionRequest datos = mock(VerificarRecuperacionRequest.class);
        controller.verificar(datos); verify(servicio).verificar(datos);
    }
    @Test void confirmarDelegaAlServicio() {
        ConfirmarRecuperacionRequest datos = mock(ConfirmarRecuperacionRequest.class);
        controller.confirmar(datos); verify(servicio).confirmar(datos);
    }
}
