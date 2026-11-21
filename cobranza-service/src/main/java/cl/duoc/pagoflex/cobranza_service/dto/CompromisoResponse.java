package cl.duoc.pagoflex.cobranza_service.dto;

public record CompromisoResponse(
    Long id,
    Long usuarioId,
    String folio,
    String empresa,
    String concepto,
    Long monto,
    Long recargo,
    String fechaVencimiento,
    String estado,
    Integer numeroCuota,
    Integer totalCuotas,
    String referenciaPlan
) {
}