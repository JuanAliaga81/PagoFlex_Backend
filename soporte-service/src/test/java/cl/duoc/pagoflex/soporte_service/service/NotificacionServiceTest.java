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

class NotificacionServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final NotificacionRepository repo=mock(NotificacionRepository.class);
    private final NotificacionService servicio=new NotificacionService(repo);
    private Notificacion aviso(){var n=new Notificacion(); n.setUsuarioId(1L); n.setTipo(TipoNotificacion.REPORTE); n.setTitulo("Aviso"); n.setMensaje("Mensaje"); n.setLeida(false); n.setFecha(Instant.now()); return n;}
    @Test void crea() {
        when(repo.save(any(Notificacion.class))).thenAnswer(i->i.getArgument(0)); var r=servicio.crear(new CrearNotificacionRequest(1L,"REPORTE"," Aviso "," Mensaje ","F1")); assertEquals("Aviso",r.titulo()); assertFalse(r.leida()); assertEquals("REPORTE",r.tipo());
    }
    @Test void rechazaDatos() {
        error(400,()->servicio.crear(null)); error(400,()->servicio.crear(new CrearNotificacionRequest(0L,"REPORTE","A","M",null))); error(400,()->servicio.crear(new CrearNotificacionRequest(1L,null,"A","M",null))); error(400,()->servicio.crear(new CrearNotificacionRequest(1L,"INVALIDO","A","M",null))); error(400,()->servicio.crearInterna(1L,null,"A","M",null)); error(400,()->servicio.crearInterna(1L,TipoNotificacion.REPORTE,"","M",null)); error(400,()->servicio.crearInterna(1L,TipoNotificacion.REPORTE,"A","",null)); error(400,()->servicio.crearInterna(1L,TipoNotificacion.REPORTE,"A","M","x".repeat(81)));
    }
    @Test void consulta() {
        when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(aviso())); when(repo.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(1L); assertEquals(1,servicio.listar(1L).size()); assertEquals(1L,servicio.noLeidas(1L)); error(400,()->servicio.listar(0L));
    }
    @Test void marcaLeida() {
        var n=aviso(); when(repo.findByIdAndUsuarioId(2L,1L)).thenReturn(Optional.of(n)); when(repo.save(n)).thenReturn(n); assertTrue(servicio.marcarLeida(1L,2L).leida()); error(404,()->servicio.marcarLeida(1L,3L));
    }
    @Test void marcaTodas() {
        var a=aviso(); var b=aviso(); when(repo.findByUsuarioIdAndLeidaFalse(1L)).thenReturn(List.of(a,b)); assertEquals(2,servicio.marcarTodas(1L)); assertTrue(a.getLeida()); assertTrue(b.getLeida()); verify(repo).saveAll(List.of(a,b));
    }
}
