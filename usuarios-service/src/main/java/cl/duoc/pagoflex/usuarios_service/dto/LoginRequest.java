package cl.duoc.pagoflex.usuarios_service.dto;

public record LoginRequest(
    String correo,
    String contrasena
) {
}