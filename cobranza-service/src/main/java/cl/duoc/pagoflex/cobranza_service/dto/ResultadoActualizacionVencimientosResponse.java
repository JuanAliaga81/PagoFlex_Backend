
package cl.duoc.pagoflex.cobranza_service.dto;

import java.util.List;

public record ResultadoActualizacionVencimientosResponse(
    Integer cantidadActualizada,
    List<String> sinConfigurar,
    Boolean correcto
) {
}
