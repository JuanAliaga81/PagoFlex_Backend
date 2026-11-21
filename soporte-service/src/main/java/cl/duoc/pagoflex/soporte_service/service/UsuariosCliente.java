package cl.duoc.pagoflex.soporte_service.service;

import cl.duoc.pagoflex.soporte_service.dto.UsuarioRemotoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuariosCliente {
    private final RestClient cliente;

    public UsuariosCliente(@Value("${usuarios.service.url}") String url) {
        this.cliente = RestClient.builder().baseUrl(url).build();
    }

    public UsuarioRemotoResponse obtener(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario inválido");
        }
        try {
            UsuarioRemotoResponse usuario = cliente.get()
                .uri("/api/usuarios/{id}", id)
                .retrieve()
                .body(UsuarioRemotoResponse.class);
            if (usuario == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía de Usuarios");
            }
            return usuario;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Usuarios respondió con error");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Usuarios no disponible");
        }
    }

    public void validarPagador(Long id) {
        UsuarioRemotoResponse u = obtener(id);
        if (!Boolean.TRUE.equals(u.activo()) || !"USUARIO_FINAL".equals(u.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no habilitada como usuario final");
        }
    }

    public void validarAgente(Long id) {
        UsuarioRemotoResponse u = obtener(id);
        if (!Boolean.TRUE.equals(u.activo()) || !"AGENTE".equals(u.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no habilitada como agente");
        }
    }
}
