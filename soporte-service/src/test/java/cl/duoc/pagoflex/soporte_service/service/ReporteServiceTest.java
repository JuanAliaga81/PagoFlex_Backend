package cl.duoc.pagoflex.soporte_service.service;

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

class ReporteServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final ReporteRepository repo=mock(ReporteRepository.class);
    private final UsuariosCliente usuarios=mock(UsuariosCliente.class);
    private final CobranzaCliente cobranza=mock(CobranzaCliente.class);
    private final NotificacionService notificaciones=mock(NotificacionService.class);
    private final ReporteService servicio=new ReporteService(repo,usuarios,cobranza,notificaciones);
    private final CrearReporteRequest datos=new CrearReporteRequest(1L,"F1","No reconozco este cobro",null);
    private Reporte reporte(){var r=new Reporte(); r.setUsuarioId(1L); r.setFolio("F1"); r.setCodigo("REP-1"); r.setEmpresa("Empresa Demo"); r.setFechaCreacion(Instant.now()); r.setEstado(EstadoReporte.PENDIENTE); return r;}
    private void compromiso(String estado){when(cobranza.obtener(1L,"F1")).thenReturn(new CompromisoRemotoResponse(1L,1L,"F1","Empresa Demo",estado));}
    @Test void crea() {
        compromiso("PENDIENTE"); var r=servicio.crear(datos); assertEquals("PENDIENTE",r.estado()); assertEquals("F1",r.folio()); verify(cobranza).abrir(1L,"F1"); verify(repo).saveAndFlush(any(Reporte.class)); verify(notificaciones).crearInterna(eq(1L),eq(TipoNotificacion.REPORTE),anyString(),anyString(),eq("F1"));
    }
    @Test void rechazaDatos() {
        error(400,()->servicio.crear(null)); error(400,()->servicio.crear(new CrearReporteRequest(0L,"F1","Otro","A"))); error(400,()->servicio.crear(new CrearReporteRequest(1L,"","Otro","A"))); error(400,()->servicio.crear(new CrearReporteRequest(1L,"F1","Invalido","A"))); error(400,()->servicio.crear(new CrearReporteRequest(1L,"F1","Otro","")));
    }
    @Test void rechazaEstadoODuplicado() {
        compromiso("PAGADO"); error(409,()->servicio.crear(datos)); compromiso("PENDIENTE"); when(repo.existsByClaveActiva("1:F1")).thenReturn(true); error(409,()->servicio.crear(datos)); verify(cobranza,never()).abrir(anyLong(),anyString());
    }
    @Test void consulta() {
        var r=reporte(); when(repo.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of(r)); when(repo.findByCodigoAndUsuarioId("REP-1",1L)).thenReturn(Optional.of(r)); when(repo.findAllByOrderByFechaCreacionDesc()).thenReturn(List.of(r)); when(repo.findByEstadoOrderByFechaCreacionAsc(EstadoReporte.PENDIENTE)).thenReturn(List.of(r)); assertEquals(1,servicio.listarUsuario(1L).size()); assertEquals("REP-1",servicio.obtenerUsuario(1L,"REP-1").codigo()); assertEquals(1,servicio.listarGestion(2L,null).size()); assertEquals(1,servicio.listarGestion(2L,EstadoReporte.PENDIENTE).size()); error(400,()->servicio.listarUsuario(0L)); error(400,()->servicio.obtenerUsuario(1L,"")); error(404,()->servicio.obtenerUsuario(1L,"otro"));
    }
    @Test void tomaYResuelve() {
        var r=reporte(); when(repo.bloquear(1L)).thenReturn(Optional.of(r)); when(repo.save(r)).thenReturn(r); assertEquals("EN_PROCESO",servicio.tomar(1L,new TomarReporteRequest(2L)).estado()); error(409,()->servicio.tomar(1L,new TomarReporteRequest(2L))); error(409,()->servicio.resolver(1L,new ResolverReporteRequest(3L,"Respuesta"))); when(cobranza.resolver(1L,"F1")).thenReturn(new CompromisoRemotoResponse(1L,1L,"F1","Empresa Demo","PENDIENTE")); assertEquals("RESUELTO",servicio.resolver(1L,new ResolverReporteRequest(2L,"Respuesta")).estado()); assertNull(r.getClaveActiva());
    }
    @Test void rechazaGestionInvalida() {
        error(400,()->servicio.tomar(1L,null)); error(400,()->servicio.resolver(1L,null)); error(400,()->servicio.resolver(1L,new ResolverReporteRequest(2L,""))); error(400,()->servicio.tomar(0L,new TomarReporteRequest(2L))); error(404,()->servicio.tomar(1L,new TomarReporteRequest(2L)));
    }
}
