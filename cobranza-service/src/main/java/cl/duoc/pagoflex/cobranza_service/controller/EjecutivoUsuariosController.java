
package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.UsuarioRemotoResponse;
import cl.duoc.pagoflex.cobranza_service.service.EjecutivoUsuariosService;
import cl.duoc.pagoflex.cobranza_service.service.EjecutivoUsuariosService.ActualizarUsuarioDatos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza/ejecutivos")
public class EjecutivoUsuariosController {

    private final EjecutivoUsuariosService
        ejecutivoUsuariosService;

    public EjecutivoUsuariosController(
        EjecutivoUsuariosService ejecutivoUsuariosService
    ) {

        this.ejecutivoUsuariosService =
            ejecutivoUsuariosService;
    }

    // ========================================
    // LISTAR USUARIOS DE UNA EMPRESA
    // ========================================

    @GetMapping("/{ejecutivoId}/empresas/{empresaId}/usuarios")
    public List<UsuarioRemotoResponse> listarUsuarios(

        @PathVariable("ejecutivoId")
        Long ejecutivoId,

        @PathVariable("empresaId")
        Long empresaId

    ) {

        return ejecutivoUsuariosService.listarUsuarios(
            ejecutivoId,
            empresaId
        );
    }

    // ========================================
    // MODIFICAR USUARIO DE UNA EMPRESA
    // ========================================

    @PutMapping(
        "/{ejecutivoId}/empresas/{empresaId}/usuarios/{usuarioId}"
    )
    public UsuarioRemotoResponse actualizarUsuario(

        @PathVariable("ejecutivoId")
        Long ejecutivoId,

        @PathVariable("empresaId")
        Long empresaId,

        @PathVariable("usuarioId")
        Long usuarioId,

        @RequestBody
        ActualizarUsuarioDatos datos

    ) {

        return ejecutivoUsuariosService.actualizarUsuario(
            ejecutivoId,
            empresaId,
            usuarioId,
            datos
        );
    }
}
