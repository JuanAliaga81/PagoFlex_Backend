package cl.duoc.pagoflex.soporte_service.dto;

import java.time.Instant;

public record ReporteResponse(
    Long id,
    String codigo,
    Long usuarioId,
    String folio,
    String empresa,
    String motivo,
    String comentario,
    String estado,
    Long agenteId,
    Instant fechaCreacion,
    Instant fechaTomado,
    String respuesta,
    String resultadoCompromiso,
    Instant fechaResolucion,
    Boolean atrasado
) {}
