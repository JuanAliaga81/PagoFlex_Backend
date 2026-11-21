package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.RevisionCompromisoRequest;
import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.service.RevisionCompromisoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/cobranza/revisiones")
public class RevisionCompromisoController {
    private final RevisionCompromisoService servicio;
    private final String claveInterna;

    public RevisionCompromisoController(RevisionCompromisoService servicio,
        @Value("${PAGOFLEX_INTERNAL_KEY:pagoflex-solo-desarrollo-local}") String claveInterna) {
        this.servicio = servicio;
        this.claveInterna = claveInterna;
    }

    @PostMapping("/abrir")
    public ResultadoRevision abrir(@RequestHeader("X-PagoFlex-Internal-Key") String clave,
                                    @RequestBody RevisionCompromisoRequest datos) {
        validarClave(clave);
        return convertir(servicio.abrir(datos));
    }

    @PostMapping("/resolver")
    public ResultadoRevision resolver(@RequestHeader("X-PagoFlex-Internal-Key") String clave,
                                       @RequestBody RevisionCompromisoRequest datos) {
        validarClave(clave);
        return convertir(servicio.resolver(datos));
    }

    private void validarClave(String clave) {
        if (!MessageDigest.isEqual(claveInterna.getBytes(StandardCharsets.UTF_8),
                                    clave.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso interno no autorizado");
        }
    }

    private ResultadoRevision convertir(Compromiso c) {
        return new ResultadoRevision(c.getId(), c.getUsuarioId(), c.getFolio(),
            c.getEmpresa(), c.getEstado().name());
    }

    public record ResultadoRevision(Long id, Long usuarioId, String folio,
                                    String empresa, String estado) {}
}
