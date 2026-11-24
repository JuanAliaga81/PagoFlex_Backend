package cl.duoc.pagoflex.cobranza_service.service;

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

class AnulacionServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final AnulacionRepository anulaciones=mock(AnulacionRepository.class);
    private final CompromisoRepository repo=mock(CompromisoRepository.class);
    private final EjecutivoEmpresaRepository asignaciones=mock(EjecutivoEmpresaRepository.class);
    private final PagoRepository pagos=mock(PagoRepository.class);
    private final AnulacionService servicio=new AnulacionService(anulaciones,repo,asignaciones,pagos,"http://localhost:1");
    private final RestClient cliente=mock(RestClient.class);
    private final RestClient.RequestHeadersUriSpec<?> get = mock(RestClient.RequestHeadersUriSpec.class);
    private RestClient.ResponseSpec respuestaUsuario(Long id) {
        RestClient.RequestHeadersSpec<?> headers = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respuesta = mock(RestClient.ResponseSpec.class);
        doReturn(headers).when(get).uri("/api/usuarios/{id}", id);
        when(headers.retrieve()).thenReturn(respuesta);
        return respuesta;
    }
    private final AnularCompromisoRequest datos=new AnularCompromisoRequest(2L,1L,"F1","Error de registro",null);
    @BeforeEach void prepararCliente(){ReflectionTestUtils.setField(servicio,"usuariosClient",cliente); doReturn(get).when(cliente).get();}
    private void ejecutivo(String rol,boolean activo){when(respuestaUsuario(2L).body(UsuarioRemotoResponse.class)).thenReturn(new UsuarioRemotoResponse(2L,"Prueba","p@example.com","123456785",rol,null,activo));}
    private Compromiso preparar(){ejecutivo("ADMINISTRADOR",true); var e=new Empresa(); e.setNombre("Empresa Demo"); var a=new EjecutivoEmpresa(); a.setEmpresa(e); when(asignaciones.buscarEmpresaAsignada(2L)).thenReturn(Optional.of(a)); var c=compromiso(); when(repo.bloquearParaPago(1L,"F1")).thenReturn(Optional.of(c)); return c;}
    private Compromiso compromiso() {
        Compromiso c = new Compromiso();
        c.setUsuarioId(1L); c.setFolio("F1"); c.setEmpresa("Empresa Demo");
        c.setConcepto("Mensualidad"); c.setMonto(1000L); c.setRecargo(0L);
        c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2));
        c.setEstado(EstadoCompromiso.PENDIENTE); return c;
    }
    @Test void anula() {
        var c=preparar(); when(anulaciones.saveAndFlush(any(Anulacion.class))).thenAnswer(i->i.getArgument(0)); assertNotNull(servicio.anularCompromiso(datos)); assertEquals(EstadoCompromiso.ANULADO,c.getEstado()); verify(repo).saveAndFlush(c);
    }
    @Test void rechazaDatos() {
        error(400,()->servicio.anularCompromiso(null)); error(400,()->servicio.anularCompromiso(new AnularCompromisoRequest(0L,1L,"F1","Error de registro",null))); error(400,()->servicio.anularCompromiso(new AnularCompromisoRequest(2L,1L,"","Error de registro",null))); error(400,()->servicio.anularCompromiso(new AnularCompromisoRequest(2L,1L,"F1","Invalido",null))); error(400,()->servicio.anularCompromiso(new AnularCompromisoRequest(2L,1L,"F1","Otro","corto"))); error(400,()->servicio.anularCompromiso(new AnularCompromisoRequest(2L,1L,"F1","Otro","x".repeat(301))));
    }
    @Test void rechazaRolYAsignacion() {
        ejecutivo("USUARIO_FINAL",true); error(403,()->servicio.anularCompromiso(datos)); ejecutivo("ADMINISTRADOR",false); error(403,()->servicio.anularCompromiso(datos)); ejecutivo("ADMINISTRADOR",true); error(403,()->servicio.anularCompromiso(datos));
    }
    @Test void rechazaEmpresaAjena() {
        var c=preparar(); c.setEmpresa("Otra"); error(403,()->servicio.anularCompromiso(datos));
    }
    @Test void rechazaEstadoYPago() {
        var c=preparar(); c.setEstado(EstadoCompromiso.PAGADO); error(409,()->servicio.anularCompromiso(datos)); c.setEstado(EstadoCompromiso.PENDIENTE); when(pagos.existsByCompromisoId(c.getId())).thenReturn(true); error(409,()->servicio.anularCompromiso(datos)); when(pagos.existsByCompromisoId(c.getId())).thenReturn(false); when(anulaciones.existsByCompromisoId(c.getId())).thenReturn(true); error(409,()->servicio.anularCompromiso(datos));
    }
    @Test void consulta() {
        var c=preparar(); var a=new Anulacion(); a.setCompromiso(c); a.setEjecutivoId(2L); a.setFecha(Instant.now()); when(anulaciones.buscarPorCompromiso(1L,"F1")).thenReturn(Optional.of(a)); assertNotNull(servicio.obtenerAnulacion(2L,1L,"F1")); error(400,()->servicio.obtenerAnulacion(0L,1L,"F1")); error(404,()->servicio.obtenerAnulacion(2L,1L,"F2"));
    }
    @Test void servicioUsuariosNoDisponible(){when(respuestaUsuario(2L).body(UsuarioRemotoResponse.class)).thenThrow(new org.springframework.web.client.ResourceAccessException("sin red")); error(503,()->servicio.anularCompromiso(datos));}
    @Test void compromisoInexistente(){preparar(); when(repo.bloquearParaPago(1L,"F1")).thenReturn(Optional.empty()); error(404,()->servicio.anularCompromiso(datos));}
}
