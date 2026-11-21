package cl.duoc.pagoflex.soporte_service.dto;

public record CrearReporteRequest(
    Long usuarioId,
    String folio,
    String motivo,
    String comentario
) {}
