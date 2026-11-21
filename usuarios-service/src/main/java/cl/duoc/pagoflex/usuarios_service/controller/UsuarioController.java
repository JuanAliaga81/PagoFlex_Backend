
package cl.duoc.pagoflex.usuarios_service.controller;

import cl.duoc.pagoflex.usuarios_service.dto.ActualizarUsuarioRequest;
import cl.duoc.pagoflex.usuarios_service.dto.CambiarContrasenaRequest;
import cl.duoc.pagoflex.usuarios_service.dto.LoginRequest;
import cl.duoc.pagoflex.usuarios_service.dto.RegistroRequest;
import cl.duoc.pagoflex.usuarios_service.dto.UsuarioResponse;
import cl.duoc.pagoflex.usuarios_service.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(
        UsuarioService usuarioService
    ) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar(
        @RequestParam(required = false)
        String buscar
    ) {

        return usuarioService.listar(buscar);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(
        @PathVariable Long id
    ) {

        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(
        @RequestBody RegistroRequest datos
    ) {

        return usuarioService.registrar(datos);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(
        @RequestBody LoginRequest datos
    ) {

        return usuarioService.iniciarSesion(datos);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
        @PathVariable Long id,
        @RequestBody ActualizarUsuarioRequest datos
    ) {

        return usuarioService.actualizar(
            id,
            datos
        );
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(
        @PathVariable Long id,
        @RequestBody CambiarContrasenaRequest datos
    ) {

        usuarioService.cambiarContrasena(
            id,
            datos
        );
    }
}
