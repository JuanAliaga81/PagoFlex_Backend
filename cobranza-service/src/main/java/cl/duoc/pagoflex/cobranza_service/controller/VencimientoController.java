
package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.ResultadoActualizacionVencimientosResponse;
import cl.duoc.pagoflex.cobranza_service.dto.VencimientoDetectadoResponse;
import cl.duoc.pagoflex.cobranza_service.service.VencimientoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza/vencimientos")
public class VencimientoController {

    private final VencimientoService vencimientoService;

    public VencimientoController(
        VencimientoService vencimientoService
    ) {

        this.vencimientoService =
            vencimientoService;
    }

    @GetMapping("/detectar")
    public List<VencimientoDetectadoResponse>
    detectarVencidos(
        @RequestParam Long usuarioId
    ) {

        return vencimientoService.detectarVencidos(
            usuarioId
        );
    }

    @PostMapping("/actualizar")
    public ResultadoActualizacionVencimientosResponse
    actualizarVencimientos(
        @RequestParam Long usuarioId
    ) {

        return vencimientoService.actualizarVencimientos(
            usuarioId
        );
    }
}
