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

class CobranzaControllerTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CobranzaService cobranzaService = mock(CobranzaService.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final CobranzaController controller = new CobranzaController(cobranzaService, jdbcTemplate);
    @Test void registrarCompromisoDelegaAlServicio() {
        RegistrarCompromisoRequest datos = mock(RegistrarCompromisoRequest.class);
        CompromisoResponse esperado = mock(CompromisoResponse.class); when(cobranzaService.registrarCompromiso(datos)).thenReturn(esperado); assertSame(esperado, controller.registrarCompromiso(datos)); verify(cobranzaService).registrarCompromiso(datos);
    }
    @Test void listarCompromisosDelegaAlServicio() {
        Long usuarioId = 1L;
        List<CompromisoResponse> esperado = List.of(); when(cobranzaService.listarCompromisos(usuarioId)).thenReturn(esperado); assertSame(esperado, controller.listarCompromisos(usuarioId)); verify(cobranzaService).listarCompromisos(usuarioId);
    }
    @Test void obtenerCompromisoDelegaAlServicio() {
        String folio = "prueba";
        Long usuarioId = 1L;
        CompromisoResponse esperado = mock(CompromisoResponse.class); when(cobranzaService.obtenerCompromiso(usuarioId, folio)).thenReturn(esperado); assertSame(esperado, controller.obtenerCompromiso(folio, usuarioId)); verify(cobranzaService).obtenerCompromiso(usuarioId, folio);
    }
    @Test void listarEmpresasDelegaAlServicio() {
        List<String> esperado = List.of(); when(cobranzaService.listarEmpresas()).thenReturn(esperado); assertSame(esperado, controller.listarEmpresas()); verify(cobranzaService).listarEmpresas();
    }
    @Test void rechazaEjecutivoInvalido() {
        error(400, () -> controller.registrarCompromisoEjecutivo(0L, null)); verifyNoInteractions(jdbcTemplate, cobranzaService);
    }
    @Test void registraEjecutivoAutorizado() {
        var datos = new RegistrarCompromisoRequest(1L,"Empresa Demo","Mensualidad",1000L,"01-01-2099",null,null,null);
        var esperado = mock(CompromisoResponse.class);
        when(jdbcTemplate.queryForObject(anyString(),eq(Integer.class),eq(2L),eq("Empresa Demo"))).thenReturn(1);
        when(cobranzaService.registrarCompromiso(datos)).thenReturn(esperado);
        assertSame(esperado,controller.registrarCompromisoEjecutivo(2L,datos));
    }
    @Test void rechazaEmpresaAjena() {
        var datos = new RegistrarCompromisoRequest(1L,"Empresa Demo","Mensualidad",1000L,"01-01-2099",null,null,null);
        when(jdbcTemplate.queryForObject(anyString(),eq(Integer.class),eq(2L),eq("Empresa Demo"))).thenReturn(0);
        error(403,()->controller.registrarCompromisoEjecutivo(2L,datos));
        error(400,()->controller.registrarCompromisoEjecutivo(2L,null));
        verifyNoInteractions(cobranzaService);
    }
}
