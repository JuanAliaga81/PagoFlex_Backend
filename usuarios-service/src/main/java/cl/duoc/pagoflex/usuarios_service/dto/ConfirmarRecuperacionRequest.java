
package cl.duoc.pagoflex.usuarios_service.dto;

public record ConfirmarRecuperacionRequest(
    String correo,
    String codigo,
    String nuevaContrasena
) {
}
