package cl.duoc.pagoflex.usuarios_service.dto;

public record RegistroRequest(
    String nombre,
    String correo,
    String rut,
    String contrasena,
    String confirmacion
) {
}
