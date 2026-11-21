package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.ConfigurarRecargoRequest;
import cl.duoc.pagoflex.cobranza_service.dto.EmpresaRecargoResponse;
import cl.duoc.pagoflex.cobranza_service.service.VencimientoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cobranza/empresas")
public class EmpresaRecargoController {

    private final VencimientoService vencimientoService;

    public EmpresaRecargoController(
        VencimientoService vencimientoService
    ) {

        this.vencimientoService =
            vencimientoService;
    }

    @GetMapping("/{id}/recargo")
    public EmpresaRecargoResponse obtenerRecargo(
        @PathVariable Long id
    ) {

        return vencimientoService.obtenerRecargoEmpresa(
            id
        );
    }

    @PutMapping("/{id}/recargo")
    public EmpresaRecargoResponse configurarRecargo(
        @PathVariable Long id,
        @RequestBody ConfigurarRecargoRequest datos
    ) {

        return vencimientoService.configurarRecargo(
            id,
            datos
        );
    }
}
