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

class UsuariosClienteTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final UsuariosCliente servicio=new UsuariosCliente("http://localhost:1");
    private final RestClient.Builder builder=RestClient.builder().baseUrl("http://localhost:1");
    private final org.springframework.test.web.client.MockRestServiceServer servidor=org.springframework.test.web.client.MockRestServiceServer.bindTo(builder).build();
    @BeforeEach void preparar(){ReflectionTestUtils.setField(servicio,"cliente",builder.build());}
    private void respuesta(String rol,boolean activo,int veces){
        String json = "{\"id\":1,\"nombre\":\"Prueba\",\"rol\":\""+rol+"\",\"activo\":"+activo+"}";
        servidor.expect(org.springframework.test.web.client.ExpectedCount.times(veces),org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:1/api/usuarios/1"))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(json,org.springframework.http.MediaType.APPLICATION_JSON));
    }
    @Test void validaPagador(){respuesta("USUARIO_FINAL",true,3); assertEquals(1L,servicio.obtener(1L).id()); assertDoesNotThrow(()->servicio.validarPagador(1L)); error(403,()->servicio.validarAgente(1L)); servidor.verify();}
    @Test void validaAgente(){respuesta("AGENTE",true,2); assertDoesNotThrow(()->servicio.validarAgente(1L)); error(403,()->servicio.validarPagador(1L)); servidor.verify();}
    @Test void cuentaInactiva(){respuesta("AGENTE",false,1); error(403,()->servicio.validarAgente(1L)); servidor.verify();}
    @Test void rechazaId(){error(400,()->servicio.obtener(0L));}
    @Test void respuestaVacia(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:1/api/usuarios/1")).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess()); error(502,()->servicio.obtener(1L)); servidor.verify();}
    @Test void noExiste(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:1/api/usuarios/1")).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.NOT_FOUND)); error(404,()->servicio.obtener(1L)); servidor.verify();}
    @Test void errorRemoto(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:1/api/usuarios/1")).andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withServerError()); error(502,()->servicio.obtener(1L)); servidor.verify();}
    @Test void noDisponible(){servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:1/api/usuarios/1")).andRespond(r->{throw new java.io.IOException("sin red");}); error(503,()->servicio.obtener(1L)); servidor.verify();}
}
