package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.AnulacionResponse;
import cl.duoc.pagoflex.cobranza_service.dto.AnularCompromisoRequest;
import cl.duoc.pagoflex.cobranza_service.service.AnulacionService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cobranza/anulaciones")
public class AnulacionController {

    private final AnulacionService anulacionService;

    public AnulacionController(
        AnulacionService anulacionService
    ) {

        this.anulacionService =
            anulacionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnulacionResponse anularCompromiso(
        @RequestBody AnularCompromisoRequest datos
    ) {

        return anulacionService.anularCompromiso(
            datos
        );
    }

    @GetMapping("/{folio}")
    public AnulacionResponse obtenerAnulacion(
        @PathVariable String folio,
        @RequestParam Long ejecutivoId,
        @RequestParam Long usuarioId
    ) {

        return anulacionService.obtenerAnulacion(
            ejecutivoId,
            usuarioId,
            folio
        );
    }
}
