package cl.duoc.pagoflex.cobranza_service.dto;

public record RegistrarCompromisoRequest(
    Long usuarioId,
    String empresa,
    String concepto,
    Long monto,
    String fechaVencimiento,
    Integer numeroCuota,
    Integer totalCuotas,
    String referenciaPlan
) {
}