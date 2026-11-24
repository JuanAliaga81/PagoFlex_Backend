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

class CobranzaServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CompromisoRepository repo=mock(CompromisoRepository.class);
    private final EmpresaRepository empresas=mock(EmpresaRepository.class);
    private final CobranzaService servicio=new CobranzaService(repo,empresas);
    private final String fecha=LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
    private RegistrarCompromisoRequest datos(Long id,String empresa,String concepto,Long monto,String fecha,Integer n,Integer t,String plan) { return new RegistrarCompromisoRequest(id,empresa,concepto,monto,fecha,n,t,plan); }
    private Compromiso compromiso() {
        Compromiso c = new Compromiso();
        c.setUsuarioId(1L); c.setFolio("F1"); c.setEmpresa("Empresa Demo");
        c.setConcepto("Mensualidad"); c.setMonto(1000L); c.setRecargo(0L);
        c.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2));
        c.setEstado(EstadoCompromiso.PENDIENTE); return c;
    }
    @Test void registraNuevaEmpresa() {
        when(empresas.save(any(Empresa.class))).thenAnswer(i->i.getArgument(0)); when(repo.save(any(Compromiso.class))).thenAnswer(i->i.getArgument(0)); var r=servicio.registrarCompromiso(datos(1L," Empresa Demo ","Mensualidad",1000L,fecha,null,null,null)); assertEquals("Empresa Demo",r.empresa()); assertEquals("PENDIENTE",r.estado()); assertEquals(1000L,r.monto()); assertTrue(r.folio().startsWith("CP-"));
    }
    @Test void registraCuota() {
        var e=new Empresa(); e.setNombre("Empresa Demo"); when(empresas.findByNombreIgnoreCase("Empresa Demo")).thenReturn(Optional.of(e)); when(repo.save(any(Compromiso.class))).thenAnswer(i->i.getArgument(0)); var r=servicio.registrarCompromiso(datos(1L,"Empresa Demo","Mensualidad",1000L,fecha,1,2,"Plan")); assertEquals(1,r.numeroCuota()); assertEquals(2,r.totalCuotas()); verify(empresas,never()).save(any());
    }
    @Test void rechazaDatos() {
        error(400,()->servicio.registrarCompromiso(null)); error(400,()->servicio.registrarCompromiso(datos(0L,"Empresa","Mensualidad",1000L,fecha,null,null,null))); error(400,()->servicio.registrarCompromiso(datos(1L,"","Mensualidad",1000L,fecha,null,null,null))); error(400,()->servicio.registrarCompromiso(datos(1L,"Empresa","X",1000L,fecha,null,null,null))); error(400,()->servicio.registrarCompromiso(datos(1L,"Empresa","Mensualidad",0L,fecha,null,null,null)));
    }
    @Test void rechazaFechas() {
        for(String f:List.of("","31-02-2026","01-01-2000")) error(400,()->servicio.registrarCompromiso(datos(1L,"Empresa","Mensualidad",1000L,f,null,null,null)));
    }
    @Test void rechazaCuotas() {
        error(400,()->servicio.registrarCompromiso(datos(1L,"Empresa","Mensualidad",1000L,fecha,1,null,null))); error(400,()->servicio.registrarCompromiso(datos(1L,"Empresa","Mensualidad",1000L,fecha,3,2,"Plan"))); when(repo.existsByUsuarioIdAndEmpresaIgnoreCaseAndConceptoIgnoreCaseAndNumeroCuota(1L,"Empresa","Mensualidad",1)).thenReturn(true); error(409,()->servicio.registrarCompromiso(datos(1L,"Empresa","Mensualidad",1000L,fecha,1,2,"Plan")));
    }
    @Test void consulta() {
        var c=compromiso(); when(repo.findByUsuarioIdOrderByFechaVencimientoAscIdAsc(1L)).thenReturn(List.of(c)); when(repo.findByUsuarioIdAndFolio(1L,"F1")).thenReturn(Optional.of(c)); assertEquals(1,servicio.listarCompromisos(1L).size()); assertEquals("F1",servicio.obtenerCompromiso(1L," F1 ").folio()); error(400,()->servicio.listarCompromisos(0L)); error(400,()->servicio.obtenerCompromiso(1L,"")); error(404,()->servicio.obtenerCompromiso(1L,"F2"));
    }
    @Test void listaEmpresas() {
        var e=new Empresa(); e.setNombre("Empresa Demo"); when(empresas.findAllByOrderByNombreAsc()).thenReturn(List.of(e)); assertEquals(List.of("Empresa Demo"),servicio.listarEmpresas());
    }
}
