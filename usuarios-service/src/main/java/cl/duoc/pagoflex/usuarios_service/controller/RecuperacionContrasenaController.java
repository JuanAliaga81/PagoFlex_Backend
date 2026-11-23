
package cl.duoc.pagoflex.usuarios_service.controller;

import cl.duoc.pagoflex.usuarios_service.dto.ConfirmarRecuperacionRequest;
import cl.duoc.pagoflex.usuarios_service.dto.SolicitarRecuperacionRequest;
import cl.duoc.pagoflex.usuarios_service.dto.VerificarRecuperacionRequest;

import cl.duoc.pagoflex.usuarios_service.service.RecuperacionContrasenaService;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/recuperacion")
public class RecuperacionContrasenaController {

    private final RecuperacionContrasenaService servicio;

    public RecuperacionContrasenaController(
        RecuperacionContrasenaService servicio
    ) {

        this.servicio = servicio;
    }

    // ========================================
    // 1. ENVIAR CODIGO POR CORREO
    // ========================================

    @PostMapping("/solicitar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void solicitar(
        @RequestBody SolicitarRecuperacionRequest datos
    ) {

        servicio.solicitar(datos);
    }

    // ========================================
    // 2. VERIFICAR CODIGO
    // ========================================

    @PostMapping("/verificar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verificar(
        @RequestBody VerificarRecuperacionRequest datos
    ) {

        servicio.verificar(datos);
    }

    // ========================================
    // 3. ACTUALIZAR CONTRASEÑA
    // ========================================

    @PostMapping("/confirmar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmar(
        @RequestBody ConfirmarRecuperacionRequest datos
    ) {

        servicio.confirmar(datos);
    }
}
