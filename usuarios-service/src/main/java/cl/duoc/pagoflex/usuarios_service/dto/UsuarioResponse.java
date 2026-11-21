package cl.duoc.pagoflex.usuarios_service.dto;

public record UsuarioResponse(
    Long id,
    String nombre,
    String correo,
    String rut,
    String rol,
    String fotoPerfil,
    Boolean activo
) {
}