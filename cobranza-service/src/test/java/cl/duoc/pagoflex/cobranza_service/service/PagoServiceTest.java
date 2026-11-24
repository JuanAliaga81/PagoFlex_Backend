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

class PagoServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CompromisoRepository repo=mock(CompromisoRepository.class);
    private final PagoRepository pagos=mock(PagoRepository.class);
    private final PagoService servicio=new PagoService(pagos,repo);
    private final RegistrarPagoRequest datos=new RegistrarPagoRequest(1L,"F1","Tarjeta",1000L);
    private Compromiso preparar() { var c=compromiso(); when(repo.bloquearParaPago(1L,"F1")).thenReturn(Optional.of(c)); return c; }
    private Compromiso compromiso() {
        Compromiso c = new Compromiso();
        c.setId(1L); c.setUsuarioId(1L); c.setFolio("F1"); c.setEmpresa("Empresa Demo");
        c.setConcepto("Mensualidad"); c.setMonto(1000L); c.setRecargo(0L);
        c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2));
        c.setEstado(EstadoCompromiso.PENDIENTE); return c;
    }
    @Test void paga() {
        var c=preparar(); var r=servicio.registrarPago(datos); assertEquals(1000L,r.montoTotal()); assertEquals(EstadoCompromiso.PAGADO,c.getEstado()); verify(pagos).saveAndFlush(any(Pago.class)); verify(repo).saveAndFlush(c);
    }
    @Test void solicitudesInvalidas() {
        error(400,()->servicio.registrarPago(null)); error(400,()->servicio.registrarPago(new RegistrarPagoRequest(0L,"F1","Tarjeta",1000L))); error(400,()->servicio.registrarPago(new RegistrarPagoRequest(1L,"","Tarjeta",1000L))); error(400,()->servicio.registrarPago(new RegistrarPagoRequest(1L,"F1","",1000L))); error(400,()->servicio.registrarPago(new RegistrarPagoRequest(1L,"F1","Tarjeta",0L)));
    }
    @Test void noExiste() {
        error(404,()->servicio.registrarPago(datos));
    }
    @Test void estadosBloqueados() {
        var c=preparar(); for(var estado:List.of(EstadoCompromiso.PAGADO,EstadoCompromiso.ANULADO,EstadoCompromiso.EN_REVISION)){ c.setEstado(estado); error(409,()->servicio.registrarPago(datos)); } verify(pagos,never()).saveAndFlush(any());
    }
    @Test void pagoDuplicado() {
        var c=preparar(); when(pagos.existsByCompromisoId(c.getId())).thenReturn(true); error(409,()->servicio.registrarPago(datos));
    }
    @Test void fechasYRecargos() {
        var c=preparar(); c.setFechaVencimiento(null); error(409,()->servicio.registrarPago(datos)); c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1)); error(409,()->servicio.registrarPago(datos)); c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(1)); c.setEstado(EstadoCompromiso.VENCIDO); error(409,()->servicio.registrarPago(datos)); c.setEstado(EstadoCompromiso.PENDIENTE); c.setRecargo(50L); error(409,()->servicio.registrarPago(datos));
    }
    @Test void cuotasInvalidas() {
        var c=preparar(); c.setNumeroCuota(1); error(409,()->servicio.registrarPago(datos));
    }
    @Test void existeAnterior() {
        var c=preparar(); when(repo.existeCompromisoAnterior(eq(1L),eq("Empresa Demo"),eq(1L),eq(c.getFechaVencimiento()),anyList())).thenReturn(true); error(409,()->servicio.registrarPago(datos));
    }
    @Test void montosInvalidos() {
        var c=preparar(); c.setMonto(0L); error(409,()->servicio.registrarPago(datos)); c.setMonto(Long.MAX_VALUE); error(409,()->servicio.registrarPago(datos)); c.setMonto(999L); error(409,()->servicio.registrarPago(datos));
    }
    @Test void pagoVencidoConRecargo() {
        var c=preparar(); c.setEstado(EstadoCompromiso.VENCIDO); c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1)); c.setRecargo(50L); assertEquals(1050L,servicio.registrarPago(new RegistrarPagoRequest(1L,"F1","Tarjeta",1050L)).montoTotal());
    }
    @Test void historialYComprobante() {
        var p=new Pago(); p.setFechaHora(Instant.now()); p.setNumeroComprobante("CPR-1"); p.setMontoPagado(1000L); when(pagos.buscarHistorial(1L)).thenReturn(List.of(p)); when(pagos.buscarComprobante(1L,"CPR-1")).thenReturn(Optional.of(p)); assertEquals(1,servicio.obtenerHistorial(1L).size()); assertEquals("CPR-1",servicio.obtenerComprobante(1L,"CPR-1").codigo()); error(400,()->servicio.obtenerHistorial(0L)); error(400,()->servicio.obtenerComprobante(1L,"")); error(404,()->servicio.obtenerComprobante(1L,"otro"));
    }
}
