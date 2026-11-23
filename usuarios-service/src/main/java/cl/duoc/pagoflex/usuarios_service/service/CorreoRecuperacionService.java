
package cl.duoc.pagoflex.usuarios_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CorreoRecuperacionService {

    private final RestClient cliente;

    private final String apiKey;

    private final String remitente;

    public CorreoRecuperacionService(
        @Value("${BREVO_API_KEY:}") String apiKey,
        @Value("${BREVO_REMITENTE:}") String remitente
    ) {

        this.cliente = RestClient.create();

        this.apiKey = apiKey.trim();

        this.remitente = remitente.trim();
    }

    public void enviarCodigo(
        String correoDestino,
        String codigo
    ) {

        if (
            apiKey.isBlank() ||
            remitente.isBlank()
        ) {

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El envío de correos no está configurado"
            );
        }

        String mensaje =
            "Hola,\n\n" +
            "Recibimos una solicitud para recuperar " +
            "tu contraseña de PagoFlex.\n\n" +
            "Tu código de verificación es: " +
            codigo + "\n\n" +
            "Este código vence en 5 minutos.\n\n" +
            "Si no solicitaste recuperar tu " +
            "contraseña, ignora este mensaje.\n\n" +
            "PagoFlex";

        Map<String, Object> cuerpo = Map.of(

            "sender", Map.of(
                "name", "PagoFlex",
                "email", remitente
            ),

            "to", List.of(
                Map.of(
                    "email", correoDestino
                )
            ),

            "subject",
            "PagoFlex - Recuperar contraseña",

            "textContent",
            mensaje
        );

        try {

            cliente.post()
                .uri(
                    "https://api.brevo.com/v3/smtp/email"
                )
                .header(
                    "api-key",
                    apiKey
                )
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();

        } catch (RestClientException e) {

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo enviar el correo de recuperación",
                e
            );
        }
    }
}
