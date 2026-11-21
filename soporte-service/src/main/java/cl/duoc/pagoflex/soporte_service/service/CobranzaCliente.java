package cl.duoc.pagoflex.soporte_service.service;

import cl.duoc.pagoflex.soporte_service.dto.CompromisoRemotoResponse;
import cl.duoc.pagoflex.soporte_service.dto.RevisionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CobranzaCliente {
    private final RestClient cliente;
    private final String claveInterna;

    public CobranzaCliente(
        @Value("${cobranza.service.url}") String url,
        @Value("${pagoflex.internal.key}") String claveInterna
    ) {
        this.cliente = RestClient.builder().baseUrl(url).build();
        this.claveInterna = claveInterna;
    }

    public CompromisoRemotoResponse obtener(Long usuarioId, String folio) {
        try {
            CompromisoRemotoResponse c = cliente.get()
                .uri(uri -> uri.path("/api/cobranza/compromisos/{folio}")
                    .queryParam("usuarioId", usuarioId).build(folio))
                .retrieve()
                .body(CompromisoRemotoResponse.class);
            if (c == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía de Cobranza");
            }
            return c;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compromiso no encontrado");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error consultando Cobranza");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cobranza no disponible");
        }
    }

    public CompromisoRemotoResponse abrir(Long usuarioId, String folio) {
        return cambiar("abrir", usuarioId, folio);
    }

    public CompromisoRemotoResponse resolver(Long usuarioId, String folio) {
        return cambiar("resolver", usuarioId, folio);
    }

    private CompromisoRemotoResponse cambiar(String accion, Long usuarioId, String folio) {
        try {
            CompromisoRemotoResponse c = cliente.post()
                .uri("/api/cobranza/revisiones/{accion}", accion)
                .header("X-PagoFlex-Internal-Key", claveInterna)
                .body(new RevisionRequest(usuarioId, folio))
                .retrieve()
                .body(CompromisoRemotoResponse.class);
            if (c == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía de Cobranza");
            }
            return c;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compromiso no encontrado");
            }
            if (e.getStatusCode().value() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Estado del compromiso incompatible");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error actualizando Cobranza");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cobranza no disponible");
        }
    }
}
