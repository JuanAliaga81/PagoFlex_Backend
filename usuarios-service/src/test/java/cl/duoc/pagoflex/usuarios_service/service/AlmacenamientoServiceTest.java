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

class AlmacenamientoServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path temp;
    private AlmacenamientoService servicio;
    @BeforeEach void preparar() {
        String anterior=System.getProperty("user.home");
        try { System.setProperty("user.home",temp.toString()); servicio=new AlmacenamientoService(); }
        finally { System.setProperty("user.home",anterior); }
    }
    @Test void guardarLeerEliminar() {
        byte[] imagen={(byte)255,(byte)216,(byte)255,(byte)217}; String nombre=servicio.guardar(new MockMultipartFile("archivo","foto.jpg","image/jpeg",imagen)); assertArrayEquals(imagen,servicio.leer(nombre)); servicio.eliminar(nombre); error(404,()->servicio.leer(nombre)); servicio.eliminar(null);
    }
    @Test void rechazaVacio() {
        error(400,()->servicio.guardar(null)); error(400,()->servicio.guardar(new MockMultipartFile("archivo",new byte[0])));
    }
    @Test void rechazaFormato() {
        error(400,()->servicio.guardar(new MockMultipartFile("archivo","foto.png","image/png",new byte[]{1})));
    }
    @Test void rechazaContenido() {
        error(400,()->servicio.guardar(new MockMultipartFile("archivo","foto.jpg","image/jpeg",new byte[]{1,2,3,4})));
    }
    @Test void rechazaTamano() {
        error(400,()->servicio.guardar(new MockMultipartFile("archivo","foto.jpg","image/jpeg",new byte[2000001])));
    }
    @Test void rechazaRuta() {
        error(400,()->servicio.leer("../foto.jpg"));
    }
}
