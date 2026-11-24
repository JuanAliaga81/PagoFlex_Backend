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

class RecuperacionContrasenaServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final UsuarioRepository repo=mock(UsuarioRepository.class);
    private final CodigoRecuperacionRepository codigos=mock(CodigoRecuperacionRepository.class);
    private final CorreoRecuperacionService correo=mock(CorreoRecuperacionService.class);
    private final RecuperacionContrasenaService servicio=new RecuperacionContrasenaService(repo,codigos,correo);
    private final Usuario usuario=new Usuario();
    private CodigoRecuperacion codigo;
    private String solicitar() {
        usuario.setId(1L); usuario.setCorreo("prueba@example.com");
        when(repo.findByCorreoIgnoreCase("prueba@example.com")).thenReturn(Optional.of(usuario));
        servicio.solicitar(new SolicitarRecuperacionRequest("prueba@example.com"));
        var captura=org.mockito.ArgumentCaptor.forClass(CodigoRecuperacion.class);
        verify(codigos).save(captura.capture()); codigo=captura.getValue();
        when(codigos.findByUsuarioId(1L)).thenReturn(Optional.of(codigo));
        var enviado=org.mockito.ArgumentCaptor.forClass(String.class);
        verify(correo).enviarCodigo(eq("prueba@example.com"),enviado.capture());
        return enviado.getValue();
    }
    @Test void solicitaYVerifica() {
        String valor=solicitar(); assertTrue(valor.matches("[0-9]{6}")); assertDoesNotThrow(()->servicio.verificar(new VerificarRecuperacionRequest("prueba@example.com",valor)));
    }
    @Test void confirmaClave() {
        String valor=solicitar(); servicio.confirmar(new ConfirmarRecuperacionRequest("prueba@example.com",valor,"Nueva1234")); assertNotNull(usuario.getContrasenaHash()); verify(codigos).delete(codigo); verify(repo).save(usuario);
    }
    @Test void claveCorta() {
        String valor=solicitar(); error(400,()->servicio.confirmar(new ConfirmarRecuperacionRequest("prueba@example.com",valor,"corta")));
    }
    @Test void correoInexistente() {
        servicio.solicitar(new SolicitarRecuperacionRequest("prueba@example.com")); verifyNoInteractions(correo);
    }
    @Test void correoInvalido() {
        error(400,()->servicio.solicitar(null));
    }
    @Test void codigoVencido() {
        String valor=solicitar(); codigo.setVenceEn(Instant.now().minusSeconds(1)); assertThrows(ResponseStatusException.class,()->servicio.verificar(new VerificarRecuperacionRequest("prueba@example.com",valor))); verify(codigos).delete(codigo);
    }
    @Test void codigoIncorrecto() {
        String valor=solicitar(); String otro=valor.equals("000000")?"111111":"000000"; assertThrows(ResponseStatusException.class,()->servicio.verificar(new VerificarRecuperacionRequest("prueba@example.com",otro))); assertEquals(1,codigo.getIntentos());
    }
    @Test void reenviarAntesDeTiempo() {
        solicitar(); servicio.solicitar(new SolicitarRecuperacionRequest("prueba@example.com")); verify(correo,times(1)).enviarCodigo(anyString(),anyString());
    }
}
