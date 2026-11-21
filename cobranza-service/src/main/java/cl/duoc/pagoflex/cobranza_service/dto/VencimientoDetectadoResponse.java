
package cl.duoc.pagoflex.cobranza_service.dto;

public record VencimientoDetectadoResponse(
    Long compromisoId,
    Long usuarioId,
    String folio,
    String empresa,
    String concepto,
    String fechaVencimiento,
    Integer diasAtraso
) {
}
