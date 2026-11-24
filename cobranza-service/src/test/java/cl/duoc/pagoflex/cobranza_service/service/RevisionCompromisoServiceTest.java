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

class RevisionCompromisoServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CompromisoRepository repo=mock(CompromisoRepository.class);
    private final EmpresaRepository empresas=mock(EmpresaRepository.class);
    private final RevisionCompromisoService servicio=new RevisionCompromisoService(repo,empresas);
    private final RevisionCompromisoRequest datos=new RevisionCompromisoRequest(1L,"F1");
    private Compromiso preparar() { var c=compromiso(); when(repo.bloquearParaPago(1L,"F1")).thenReturn(Optional.of(c)); when(repo.saveAndFlush(c)).thenReturn(c); return c; }
    private Compromiso compromiso() {
        Compromiso c = new Compromiso();
        c.setUsuarioId(1L); c.setFolio("F1"); c.setEmpresa("Empresa Demo");
        c.setConcepto("Mensualidad"); c.setMonto(1000L); c.setRecargo(0L);
        c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2));
        c.setEstado(EstadoCompromiso.PENDIENTE); return c;
    }
    @Test void abreYResuelve() {
        var c=preparar(); assertEquals(EstadoCompromiso.EN_REVISION,servicio.abrir(datos).getEstado()); assertEquals(EstadoCompromiso.PENDIENTE,servicio.resolver(datos).getEstado());
    }
    @Test void rechazaSolicitud() {
        error(400,()->servicio.abrir(null)); error(400,()->servicio.abrir(new RevisionCompromisoRequest(0L,"F1"))); error(400,()->servicio.resolver(new RevisionCompromisoRequest(1L,""))); error(404,()->servicio.abrir(datos));
    }
    @Test void rechazaEstados() {
        var c=preparar(); error(409,()->servicio.resolver(datos)); c.setEstado(EstadoCompromiso.PAGADO); error(409,()->servicio.abrir(datos)); c.setEstado(EstadoCompromiso.ANULADO); assertSame(c,servicio.resolver(datos));
    }
    @Test void resuelveVencido() {
        var c=preparar(); c.setEstado(EstadoCompromiso.EN_REVISION); c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1)); var e=new Empresa(); e.setRecargoFijo(20L); when(empresas.findByNombreIgnoreCase("Empresa Demo")).thenReturn(Optional.of(e)); assertEquals(EstadoCompromiso.VENCIDO,servicio.resolver(datos).getEstado()); assertEquals(20L,c.getRecargo());
    }
    @Test void empresaSinRecargo() {
        var c=preparar(); c.setEstado(EstadoCompromiso.EN_REVISION); c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1)); error(409,()->servicio.resolver(datos)); var e=new Empresa(); e.setRecargoFijo(null); when(empresas.findByNombreIgnoreCase("Empresa Demo")).thenReturn(Optional.of(e)); error(409,()->servicio.resolver(datos)); e.setRecargoFijo(-1L); error(409,()->servicio.resolver(datos));
    }
}
