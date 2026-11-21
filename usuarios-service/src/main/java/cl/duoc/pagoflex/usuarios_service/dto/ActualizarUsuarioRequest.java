package cl.duoc.pagoflex.usuarios_service.dto;

public record ActualizarUsuarioRequest(
    String nombre,
    String correo
) {
}