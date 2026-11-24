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

class UsuarioServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final UsuarioRepository repo = mock(UsuarioRepository.class);
    private final UsuarioService servicio = new UsuarioService(repo);
    private RegistroRequest registro(String nombre,String correo,String rut,String clave,String confirmacion) {
        return new RegistroRequest(nombre,correo,rut,clave,confirmacion);
    }
    private Usuario registrado() {
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        servicio.registrar(registro("Persona Prueba","prueba@example.com","12345678-5","Clave1234","Clave1234"));
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture()); return captor.getValue();
    }
    @Test void registroNormalizaDatos() {
        var u=registrado(); assertEquals("123456785",u.getRut()); assertEquals("USUARIO_FINAL",u.getRol()); assertNotEquals("Clave1234",u.getContrasenaHash());
    }
    @Test void rechazaRegistroNulo() {
        error(400,()->servicio.registrar(null));
    }
    @Test void rechazaNombre() {
        error(400,()->servicio.registrar(registro("A","prueba@example.com","123456785","Clave1234","Clave1234")));
    }
    @Test void rechazaCorreo() {
        error(400,()->servicio.registrar(registro("Persona","mal","123456785","Clave1234","Clave1234")));
    }
    @Test void rechazaRut() {
        error(400,()->servicio.registrar(registro("Persona","prueba@example.com","123456780","Clave1234","Clave1234")));
    }
    @Test void rechazaClaveCorta() {
        error(400,()->servicio.registrar(registro("Persona","prueba@example.com","123456785","123","123")));
    }
    @Test void rechazaConfirmacion() {
        error(400,()->servicio.registrar(registro("Persona","prueba@example.com","123456785","Clave1234","Otra1234")));
    }
    @Test void rechazaCorreoDuplicado() {
        when(repo.existsByCorreoIgnoreCase("prueba@example.com")).thenReturn(true); error(409,()->servicio.registrar(registro("Persona","prueba@example.com","123456785","Clave1234","Clave1234")));
    }
    @Test void rechazaRutDuplicado() {
        when(repo.existsByRut("123456785")).thenReturn(true); error(409,()->servicio.registrar(registro("Persona","prueba@example.com","123456785","Clave1234","Clave1234")));
    }
    @Test void loginCorrectoEIncorrecto() {
        var u=registrado(); when(repo.findByCorreoIgnoreCase("prueba@example.com")).thenReturn(Optional.of(u)); assertEquals(u.getNombre(),servicio.iniciarSesion(new LoginRequest("PRUEBA@example.com","Clave1234")).nombre()); error(401,()->servicio.iniciarSesion(new LoginRequest("prueba@example.com","incorrecta"))); u.setActivo(false); error(403,()->servicio.iniciarSesion(new LoginRequest("prueba@example.com","Clave1234")));
    }
    @Test void loginSinCuenta() {
        error(401,()->servicio.iniciarSesion(new LoginRequest("prueba@example.com","Clave1234"))); error(401,()->servicio.iniciarSesion(null));
    }
    @Test void consultaYBusqueda() {
        var u=new Usuario(); u.setNombre("Persona"); when(repo.findAllByOrderByNombreAsc()).thenReturn(List.of(u)); when(repo.findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc("Per","Per")).thenReturn(List.of(u)); when(repo.findById(1L)).thenReturn(Optional.of(u)); assertEquals(1,servicio.listar(null).size()); assertEquals(1,servicio.listar(" Per ").size()); assertEquals("Persona",servicio.obtener(1L).nombre()); error(400,()->servicio.obtener(0L)); error(404,()->servicio.obtener(2L));
    }
    @Test void actualizar() {
        var u=new Usuario(); when(repo.findById(1L)).thenReturn(Optional.of(u)); when(repo.save(u)).thenReturn(u); assertEquals("nuevo@example.com",servicio.actualizar(1L,new ActualizarUsuarioRequest("Nuevo","NUEVO@example.com")).correo()); error(400,()->servicio.actualizar(1L,null)); when(repo.existsByCorreoIgnoreCaseAndIdNot("nuevo@example.com",1L)).thenReturn(true); error(409,()->servicio.actualizar(1L,new ActualizarUsuarioRequest("Nuevo","nuevo@example.com")));
    }
    @Test void cambiarClave() {
        var u=registrado(); when(repo.findById(1L)).thenReturn(Optional.of(u)); when(repo.findByCorreoIgnoreCase("prueba@example.com")).thenReturn(Optional.of(u)); error(400,()->servicio.cambiarContrasena(1L,null)); error(401,()->servicio.cambiarContrasena(1L,new CambiarContrasenaRequest("mal","Nueva1234"))); servicio.cambiarContrasena(1L,new CambiarContrasenaRequest("Clave1234","Nueva1234")); assertEquals(u.getNombre(),servicio.iniciarSesion(new LoginRequest("prueba@example.com","Nueva1234")).nombre());
    }
}
