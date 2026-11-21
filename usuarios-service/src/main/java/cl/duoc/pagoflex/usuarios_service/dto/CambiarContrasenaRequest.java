package cl.duoc.pagoflex.usuarios_service.dto;

public record CambiarContrasenaRequest(
    String actual,
    String nueva
) {
}