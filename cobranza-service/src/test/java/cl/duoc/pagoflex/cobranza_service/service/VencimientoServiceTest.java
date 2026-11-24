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

class VencimientoServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CompromisoRepository repo=mock(CompromisoRepository.class);
    private final EmpresaRepository empresas=mock(EmpresaRepository.class);
    private final VencimientoService servicio=new VencimientoService(repo,empresas);
    private final LocalDate hoy=LocalDate.now(ZoneId.of("America/Santiago"));
    private Compromiso preparar(){var c=compromiso(); c.setFechaVencimiento(hoy.minusDays(2)); when(repo.bloquearVencidos(1L,EstadoCompromiso.PENDIENTE,hoy)).thenReturn(List.of(c)); return c;}
    private Empresa empresa(Long tarifa){var e=new Empresa(); e.setNombre("Empresa Demo"); e.setRecargoFijo(tarifa); when(empresas.findByNombreIgnoreCase("Empresa Demo")).thenReturn(Optional.of(e)); return e;}
    private Compromiso compromiso() {
        Compromiso c = new Compromiso();
        c.setUsuarioId(1L); c.setFolio("F1"); c.setEmpresa("Empresa Demo");
        c.setConcepto("Mensualidad"); c.setMonto(1000L); c.setRecargo(0L);
        c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2));
        c.setEstado(EstadoCompromiso.PENDIENTE); return c;
    }
    @Test void detecta() {
        var c=preparar(); when(repo.findByUsuarioIdAndEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAscIdAsc(1L,EstadoCompromiso.PENDIENTE,hoy)).thenReturn(List.of(c)); assertEquals(2,servicio.detectarVencidos(1L).get(0).diasAtraso());
    }
    @Test void actualiza() {
        var c=preparar(); empresa(50L); assertEquals(1,servicio.actualizarVencimientos(1L).cantidadActualizada()); assertEquals(EstadoCompromiso.VENCIDO,c.getEstado()); assertEquals(50L,c.getRecargo()); verify(repo).save(c);
    }
    @Test void sinConfiguracion() {
        var c=preparar(); assertEquals(1,servicio.actualizarVencimientos(1L).sinConfigurar().size()); assertEquals(EstadoCompromiso.PENDIENTE,c.getEstado()); verify(repo,never()).save(any());
    }
    @Test void recargosInvalidos() {
        var c=preparar(); empresa(-1L); error(409,()->servicio.actualizarVencimientos(1L)); empresa(50L); c.setRecargo(5L); error(409,()->servicio.actualizarVencimientos(1L)); c.setRecargo(0L); c.setMonto(Long.MAX_VALUE); error(409,()->servicio.actualizarVencimientos(1L));
    }
    @Test void configura() {
        var e=empresa(20L); when(empresas.findById(1L)).thenReturn(Optional.of(e)); when(empresas.save(e)).thenReturn(e); assertEquals(20L,servicio.obtenerRecargoEmpresa(1L).recargoFijo()); assertEquals(70L,servicio.configurarRecargo(1L,new ConfigurarRecargoRequest(70L)).recargoFijo());
    }
    @Test void rechazaDatos() {
        error(400,()->servicio.detectarVencidos(0L)); error(400,()->servicio.actualizarVencimientos(null)); error(400,()->servicio.configurarRecargo(1L,null)); error(400,()->servicio.configurarRecargo(1L,new ConfigurarRecargoRequest(-1L))); error(400,()->servicio.obtenerRecargoEmpresa(0L)); error(404,()->servicio.obtenerRecargoEmpresa(1L));
    }
}
