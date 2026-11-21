package cl.duoc.pagoflex.cobranza_service.dto;

public record AnulacionResponse(
    Long id,
    Long compromisoId,
    Long usuarioId,
    String folio,
    String empresa,
    String estado,
    Long ejecutivoId,
    String motivo,
    String comentario,
    String fecha
) {
}
