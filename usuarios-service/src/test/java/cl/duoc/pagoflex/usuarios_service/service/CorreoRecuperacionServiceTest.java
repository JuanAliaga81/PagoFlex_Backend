package cl.duoc.pagoflex.usuarios_service.service;

import cl.duoc.pagoflex.usuarios_service.dto.*;
import cl.duoc.pagoflex.usuarios_service.model.*;
import cl.duoc.pagoflex.usuarios_service.repo.*;
import cl.duoc.pagoflex.usuarios_service.service.*;
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

class CorreoRecuperacionServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    @Test void sinConfiguracion() {
        error(503,()->new CorreoRecuperacionService("","").enviarCodigo("prueba@example.com","123456")); error(503,()->new CorreoRecuperacionService("clave","").enviarCodigo("prueba@example.com","123456"));
    }
    @Test void enviaCodigoSimulado(){
        var servicio=new CorreoRecuperacionService("clave-prueba","remitente@example.com");
        var builder=RestClient.builder();
        var servidor=org.springframework.test.web.client.MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(servicio,"cliente",builder.build());
        servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("https://api.brevo.com/v3/smtp/email"))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess());
        assertDoesNotThrow(()->servicio.enviarCodigo("prueba@example.com","123456")); servidor.verify();
    }
    @Test void errorDeEnvio(){
        var servicio=new CorreoRecuperacionService("clave-prueba","remitente@example.com");
        var builder=RestClient.builder();
        var servidor=org.springframework.test.web.client.MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(servicio,"cliente",builder.build());
        servidor.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("https://api.brevo.com/v3/smtp/email"))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withServerError());
        error(503,()->servicio.enviarCodigo("prueba@example.com","123456")); servidor.verify();
    }
}
