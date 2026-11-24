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

class EjecutivoUsuariosControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final EjecutivoUsuariosService servicio=mock(EjecutivoUsuariosService.class);
    private final EjecutivoUsuariosController controller=new EjecutivoUsuariosController(servicio);
    @Test void lista() {
        var esperado=List.of(new UsuarioRemotoResponse(3L,"Prueba","p@example.com","123456785","USUARIO_FINAL",null,true)); when(servicio.listarUsuarios(2L,1L)).thenReturn(esperado); assertEquals(esperado,controller.listarUsuarios(2L,1L));
    }
    @Test void actualiza() {
        var datos=new EjecutivoUsuariosService.ActualizarUsuarioDatos("Nuevo","nuevo@example.com"); var esperado=new UsuarioRemotoResponse(3L,"Nuevo","nuevo@example.com","123456785","USUARIO_FINAL",null,true); when(servicio.actualizarUsuario(2L,1L,3L,datos)).thenReturn(esperado); assertEquals(esperado,controller.actualizarUsuario(2L,1L,3L,datos));
    }
}
