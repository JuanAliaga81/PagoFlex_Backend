package cl.duoc.pagoflex.soporte_service.dto;

import java.time.Instant;

public record NotificacionResponse(
    Long id,
    Long usuarioId,
    String tipo,
    String titulo,
    String mensaje,
    String referencia,
    Boolean leida,
    Instant fecha
) {}
