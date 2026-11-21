package cl.duoc.pagoflex.soporte_service.dto;

public record UsuarioRemotoResponse(
    Long id,
    String nombre,
    String correo,
    String rut,
    String rol,
    String fotoPerfil,
    Boolean activo
) {}
