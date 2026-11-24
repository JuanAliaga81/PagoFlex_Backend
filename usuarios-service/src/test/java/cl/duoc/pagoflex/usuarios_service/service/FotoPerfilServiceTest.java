package cl.duoc.pagoflex.usuarios_service.service;

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

class FotoPerfilServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final UsuarioRepository repo=mock(UsuarioRepository.class);
    private final AlmacenamientoService almacenamiento=mock(AlmacenamientoService.class);
    private final FotoPerfilService servicio=new FotoPerfilService(repo,almacenamiento);
    @Test void guardaYReemplaza() {
        var u=new Usuario(); u.setFotoPerfil("anterior.jpg"); var f=new MockMultipartFile("foto",new byte[]{1}); when(repo.findById(1L)).thenReturn(Optional.of(u)); when(almacenamiento.guardar(f)).thenReturn("nueva.jpg"); when(repo.saveAndFlush(u)).thenReturn(u); assertEquals("nueva.jpg",servicio.guardar(1L,f).fotoPerfil()); verify(almacenamiento).eliminar("anterior.jpg");
    }
    @Test void limpiaSiFalla() {
        var u=new Usuario(); var f=new MockMultipartFile("foto",new byte[]{1}); when(repo.findById(1L)).thenReturn(Optional.of(u)); when(almacenamiento.guardar(f)).thenReturn("nueva.jpg"); when(repo.saveAndFlush(u)).thenThrow(new IllegalStateException()); assertThrows(IllegalStateException.class,()->servicio.guardar(1L,f)); verify(almacenamiento).eliminar("nueva.jpg");
    }
    @Test void obtener() {
        var u=new Usuario(); when(repo.findById(1L)).thenReturn(Optional.of(u)); error(404,()->servicio.obtener(1L)); u.setFotoPerfil("foto.jpg"); when(almacenamiento.leer("foto.jpg")).thenReturn(new byte[]{1,2}); assertArrayEquals(new byte[]{1,2},servicio.obtener(1L));
    }
    @Test void rechazaUsuario() {
        error(400,()->servicio.obtener(0L)); error(404,()->servicio.obtener(2L));
    }
}
