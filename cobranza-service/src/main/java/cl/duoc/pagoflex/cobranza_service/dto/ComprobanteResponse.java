package cl.duoc.pagoflex.cobranza_service.dto;

public record ComprobanteResponse(
    String codigo,
    String folioCompromiso,
    String empresa,
    String concepto,
    Long montoTotal,
    String fechaPago,
    Integer numeroCuota,
    Integer totalCuotas
) {
}
