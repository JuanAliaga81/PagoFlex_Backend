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

class CobranzaClienteTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final CobranzaCliente servicio=new CobranzaCliente("http://localhost:1","clave-prueba");
    private final RestClient.Builder builder=RestClient.builder().baseUrl("http://localhost:1");
    private final org.springframework.test.web.client.MockRestServiceServer servidor=org.springframework.test.web.client.MockRestServiceServer.bindTo(builder).build();
    @BeforeEach void preparar(){ReflectionTestUtils.setField(servicio,"cliente",builder.build());}
    private final String consulta="http://localhost:1/api/cobranza/compromisos/F1?usuarioId=1";
    private String revision(String accion){return "http://localhost:1/api/cobranza/revisiones/"+accion;}
    private void responder(String url,String estado){String json="{\"id\":1,\"usuarioId\":1,\"folio\":\"F1\",\"empresa\":\"Empresa Demo\",\"estado\":\""+estado+"\"}"; servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(url)).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(json,org.springframework.http.MediaType.APPLICATION_JSON));}
    @Test void consultaCompromiso(){responder(consulta,"PENDIENTE"); assertEquals("PENDIENTE",servicio.obtener(1L,"F1").estado()); servidor.verify();}
    @Test void abreYResuelve(){responder(revision("abrir"),"EN_REVISION"); responder(revision("resolver"),"PENDIENTE"); assertEquals("EN_REVISION",servicio.abrir(1L,"F1").estado()); assertEquals("PENDIENTE",servicio.resolver(1L,"F1").estado()); servidor.verify();}
    @Test void sinRespuesta(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(consulta)).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess()); error(502,()->servicio.obtener(1L,"F1")); servidor.verify();}
    @Test void noExiste(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(consulta)).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.NOT_FOUND)); error(404,()->servicio.obtener(1L,"F1")); servidor.verify();}
    @Test void estadoIncompatible(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(revision("abrir"))).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.CONFLICT)); error(409,()->servicio.abrir(1L,"F1")); servidor.verify();}
    @Test void sinConexion(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(consulta)).andRespond(r->{throw new java.io.IOException("sin red");}); error(503,()->servicio.obtener(1L,"F1")); servidor.verify();}
}
