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

class EjecutivoUsuariosServiceTest {
    private void error(int esperado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, accion).getStatusCode().value());
    }
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final EjecutivoUsuariosService servicio=new EjecutivoUsuariosService(jdbc,"http://localhost:1");
    private final RestClient cliente=mock(RestClient.class);
    private final RestClient.RequestHeadersUriSpec<?> get = mock(RestClient.RequestHeadersUriSpec.class);
    private RestClient.ResponseSpec respuestaUsuario(Long id) {
        RestClient.RequestHeadersSpec<?> headers = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respuesta = mock(RestClient.ResponseSpec.class);
        doReturn(headers).when(get).uri("/api/usuarios/{id}", id);
        when(headers.retrieve()).thenReturn(respuesta);
        return respuesta;
    }
    @BeforeEach void preparar(){ReflectionTestUtils.setField(servicio,"usuariosClient",cliente); doReturn(get).when(cliente).get();}
    private void usuario(Long id,String nombre,String rol,boolean activo){when(respuestaUsuario(id).body(UsuarioRemotoResponse.class)).thenReturn(new UsuarioRemotoResponse(id,nombre,"p@example.com","123456785",rol,null,activo));}
    private void asignar(){usuario(2L,"Ejecutivo","ADMINISTRADOR",true); when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(2L),eq(1L))).thenReturn(1);}
    private void lista(Long... ids){when(jdbc.queryForList(anyString(),eq(Long.class),eq(2L),eq(1L))).thenReturn(List.of(ids));}
    @Test void listaOrdenada() {
        asignar(); lista(3L,4L,5L); usuario(3L,"Zeta","USUARIO_FINAL",true); usuario(4L,"Ana","USUARIO_FINAL",true); usuario(5L,"Agente","AGENTE",true); assertEquals(List.of(4L,3L),servicio.listarUsuarios(2L,1L).stream().map(UsuarioRemotoResponse::id).toList());
    }
    @Test void listaVacia() {
        asignar(); lista(); assertTrue(servicio.listarUsuarios(2L,1L).isEmpty());
    }
    @Test void rechazaIds() {
        error(400,()->servicio.listarUsuarios(0L,1L)); error(400,()->servicio.listarUsuarios(2L,0L));
    }
    @Test void rechazaEmpresa() {
        usuario(2L,"Ejecutivo","ADMINISTRADOR",true); error(403,()->servicio.listarUsuarios(2L,1L));
    }
    @Test void rechazaRol() {
        usuario(2L,"Persona","USUARIO_FINAL",true); error(403,()->servicio.listarUsuarios(2L,1L)); usuario(2L,"Ejecutivo","ADMINISTRADOR",false); error(403,()->servicio.listarUsuarios(2L,1L));
    }
    @Test void actualiza() {
        asignar(); lista(3L); usuario(3L,"Anterior","USUARIO_FINAL",true); var datos=new EjecutivoUsuariosService.ActualizarUsuarioDatos("Nuevo","nuevo@example.com"); var esperado=new UsuarioRemotoResponse(3L,"Nuevo","nuevo@example.com","123456785","USUARIO_FINAL",null,true); var put=mock(RestClient.RequestBodyUriSpec.class); var cuerpo=mock(RestClient.RequestBodySpec.class); var respuesta=mock(RestClient.ResponseSpec.class);
        when(cliente.put()).thenReturn(put); when(put.uri("/api/usuarios/{id}",3L)).thenReturn(cuerpo);
        when(cuerpo.body(datos)).thenReturn(cuerpo); when(cuerpo.retrieve()).thenReturn(respuesta); when(respuesta.body(UsuarioRemotoResponse.class)).thenReturn(esperado); assertEquals(esperado,servicio.actualizarUsuario(2L,1L,3L,datos));
    }
    @Test void rechazaUsuarioAjeno() {
        asignar(); lista(3L); error(403,()->servicio.actualizarUsuario(2L,1L,4L,new EjecutivoUsuariosService.ActualizarUsuarioDatos("Nuevo","nuevo@example.com")));
    }
    @Test void rechazaDatos() {
        asignar(); lista(3L); usuario(3L,"Anterior","USUARIO_FINAL",true); error(400,()->servicio.actualizarUsuario(2L,1L,3L,null));
    }
    @Test void usuarioRemotoNoDisponible(){when(respuestaUsuario(2L).body(UsuarioRemotoResponse.class)).thenThrow(new org.springframework.web.client.ResourceAccessException("sin red")); error(503,()->servicio.listarUsuarios(2L,1L));}
    @Test void respuestaRemotaInvalida(){when(respuestaUsuario(2L).body(UsuarioRemotoResponse.class)).thenReturn(null); error(502,()->servicio.listarUsuarios(2L,1L));}
    @Test void rechazaUsuarioConRolDistinto(){asignar(); lista(3L); usuario(3L,"Agente","AGENTE",true); error(403,()->servicio.actualizarUsuario(2L,1L,3L,new EjecutivoUsuariosService.ActualizarUsuarioDatos("Nuevo","nuevo@example.com")));}
}
