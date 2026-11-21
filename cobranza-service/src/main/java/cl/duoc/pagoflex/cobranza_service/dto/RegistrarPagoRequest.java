package cl.duoc.pagoflex.cobranza_service.dto;

public record RegistrarPagoRequest(
    Long usuarioId,
    String folio,
    String medioPago,
    Long montoEsperado
) {
}