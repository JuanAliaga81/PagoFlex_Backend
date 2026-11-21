package cl.duoc.pagoflex.soporte_service.dto;

public record CompromisoRemotoResponse(
    Long id,
    Long usuarioId,
    String folio,
    String empresa,
    String estado
) {}
