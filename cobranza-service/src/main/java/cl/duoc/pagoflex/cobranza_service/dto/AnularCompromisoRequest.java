package cl.duoc.pagoflex.cobranza_service.dto;

public record AnularCompromisoRequest(
    Long ejecutivoId,
    Long usuarioId,
    String folio,
    String motivo,
    String comentario
) {
}
