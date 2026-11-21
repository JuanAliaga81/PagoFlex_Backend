package cl.duoc.pagoflex.soporte_service.dto;

public record CrearNotificacionRequest(
    Long usuarioId,
    String tipo,
    String titulo,
    String mensaje,
    String referencia
) {}
