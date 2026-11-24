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

class FotoPerfilControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final FotoPerfilService fotoPerfilService = mock(FotoPerfilService.class);
    private final FotoPerfilController controller = new FotoPerfilController(fotoPerfilService);
    @Test void subirAvatarDelegaAlServicio() {
        Long id = 1L;
        MultipartFile archivo = new MockMultipartFile("archivo", new byte[]{1});
        UsuarioResponse esperado = mock(UsuarioResponse.class); when(fotoPerfilService.guardar(id, archivo)).thenReturn(esperado); assertSame(esperado, controller.subirAvatar(id, archivo)); verify(fotoPerfilService).guardar(id, archivo);
    }
    @Test void obtenerAvatarDevuelveImagen() {
        byte[] bytes = {1,2}; when(fotoPerfilService.obtener(1L)).thenReturn(bytes); var respuesta = controller.obtenerAvatar(1L); assertArrayEquals(bytes, respuesta.getBody()); assertEquals("image/jpeg", respuesta.getHeaders().getContentType().toString());
    }
}
