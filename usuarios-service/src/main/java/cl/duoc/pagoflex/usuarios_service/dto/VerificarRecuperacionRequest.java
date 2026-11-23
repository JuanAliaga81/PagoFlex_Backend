
package cl.duoc.pagoflex.usuarios_service.dto;

public record VerificarRecuperacionRequest(
    String correo,
    String codigo
) {
}
