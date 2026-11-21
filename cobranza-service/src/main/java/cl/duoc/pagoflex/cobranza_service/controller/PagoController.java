
package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.ComprobanteResponse;
import cl.duoc.pagoflex.cobranza_service.dto.RegistrarPagoRequest;
import cl.duoc.pagoflex.cobranza_service.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(
        PagoService pagoService
    ) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComprobanteResponse registrarPago(
        @RequestBody RegistrarPagoRequest datos
    ) {

        return pagoService.registrarPago(datos);
    }

    @GetMapping
    public List<ComprobanteResponse> obtenerHistorial(
        @RequestParam Long usuarioId
    ) {

        return pagoService.obtenerHistorial(
            usuarioId
        );
    }

    @GetMapping("/{codigo}")
    public ComprobanteResponse obtenerComprobante(
        @PathVariable String codigo,
        @RequestParam Long usuarioId
    ) {

        return pagoService.obtenerComprobante(
            usuarioId,
            codigo
        );
    }
}
