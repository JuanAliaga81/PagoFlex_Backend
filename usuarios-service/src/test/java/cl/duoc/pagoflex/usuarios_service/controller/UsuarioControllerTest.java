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

class UsuarioControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private final UsuarioController controller = new UsuarioController(usuarioService);
    @Test void listarDelegaAlServicio() {
        String buscar = "prueba";
        List<UsuarioResponse> esperado = List.of(); when(usuarioService.listar(buscar)).thenReturn(esperado); assertSame(esperado, controller.listar(buscar)); verify(usuarioService).listar(buscar);
    }
    @Test void obtenerDelegaAlServicio() {
        Long id = 1L;
        UsuarioResponse esperado = mock(UsuarioResponse.class); when(usuarioService.obtener(id)).thenReturn(esperado); assertSame(esperado, controller.obtener(id)); verify(usuarioService).obtener(id);
    }
    @Test void registrarDelegaAlServicio() {
        RegistroRequest datos = mock(RegistroRequest.class);
        UsuarioResponse esperado = mock(UsuarioResponse.class); when(usuarioService.registrar(datos)).thenReturn(esperado); assertSame(esperado, controller.registrar(datos)); verify(usuarioService).registrar(datos);
    }
    @Test void iniciarSesionDelegaAlServicio() {
        LoginRequest datos = mock(LoginRequest.class);
        UsuarioResponse esperado = mock(UsuarioResponse.class); when(usuarioService.iniciarSesion(datos)).thenReturn(esperado); assertSame(esperado, controller.iniciarSesion(datos)); verify(usuarioService).iniciarSesion(datos);
    }
    @Test void actualizarDelegaAlServicio() {
        Long id = 1L;
        ActualizarUsuarioRequest datos = mock(ActualizarUsuarioRequest.class);
        UsuarioResponse esperado = mock(UsuarioResponse.class); when(usuarioService.actualizar(id, datos)).thenReturn(esperado); assertSame(esperado, controller.actualizar(id, datos)); verify(usuarioService).actualizar(id, datos);
    }
    @Test void cambiarContrasenaDelegaAlServicio() {
        Long id = 1L;
        CambiarContrasenaRequest datos = mock(CambiarContrasenaRequest.class);
        controller.cambiarContrasena(id, datos); verify(usuarioService).cambiarContrasena(id, datos);
    }
}
