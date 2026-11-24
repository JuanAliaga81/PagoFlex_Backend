
package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.UsuarioRemotoResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EjecutivoUsuariosService {

    private final JdbcTemplate jdbcTemplate;
    private final RestClient usuariosClient;

    public EjecutivoUsuariosService(
        JdbcTemplate jdbcTemplate,

        @Value("${usuarios.service.url}")
        String urlUsuarios
    ) {

        this.jdbcTemplate = jdbcTemplate;

        this.usuariosClient = RestClient.builder()
            .baseUrl(urlUsuarios)
            .build();
    }

    // ========================================
    // LISTAR USUARIOS DE LA EMPRESA
    // ========================================

    public List<UsuarioRemotoResponse> listarUsuarios(
        Long ejecutivoId,
        Long empresaId
    ) {

        validarIdentificador(
            ejecutivoId,
            "ejecutivo"
        );

        validarIdentificador(
            empresaId,
            "empresa"
        );

        validarEjecutivo(ejecutivoId);

        validarEmpresaAsignada(
            ejecutivoId,
            empresaId
        );

        List<Long> identificadores =
            obtenerUsuariosEmpresa(
                ejecutivoId,
                empresaId
            );

        List<UsuarioRemotoResponse> usuarios =
            new ArrayList<>();

        for (Long usuarioId : identificadores) {

            UsuarioRemotoResponse usuario =
                consultarUsuario(usuarioId);

            if (
                usuario != null &&
                "USUARIO_FINAL".equals(usuario.rol())
            ) {

                usuarios.add(usuario);
            }
        }

        usuarios.sort(
            Comparator.comparing(
                UsuarioRemotoResponse::nombre,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return usuarios;
    }

    // ========================================
    // MODIFICAR USUARIO DE LA EMPRESA
    // ========================================

    public UsuarioRemotoResponse actualizarUsuario(
        Long ejecutivoId,
        Long empresaId,
        Long usuarioId,
        ActualizarUsuarioDatos datos
    ) {

        validarIdentificador(
            ejecutivoId,
            "ejecutivo"
        );

        validarIdentificador(
            empresaId,
            "empresa"
        );

        validarIdentificador(
            usuarioId,
            "usuario"
        );

        validarEjecutivo(ejecutivoId);

        validarEmpresaAsignada(
            ejecutivoId,
            empresaId
        );

        // Comprobar que el usuario tiene
        // compromisos con la empresa.

        List<Long> identificadores =
            obtenerUsuariosEmpresa(
                ejecutivoId,
                empresaId
            );

        if (!identificadores.contains(usuarioId)) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El usuario no pertenece a la empresa asignada"
            );
        }

        UsuarioRemotoResponse usuario =
            consultarUsuario(usuarioId);

        if (usuario == null) {

            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"
            );
        }

        if (!"USUARIO_FINAL".equals(usuario.rol())) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo puedes modificar usuarios finales"
            );
        }

        if (
            datos == null ||
            datos.nombre() == null ||
            datos.correo() == null
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar el nombre y el correo"
            );
        }

        // Usuarios Service conserva todas sus
        // validaciones de nombre, correo y duplicados.

        try {

            UsuarioRemotoResponse actualizado =
                usuariosClient
                    .put()
                    .uri(
                        "/api/usuarios/{id}",
                        usuarioId
                    )
                    .body(datos)
                    .retrieve()
                    .body(UsuarioRemotoResponse.class);

            if (
                actualizado == null ||
                !usuarioId.equals(actualizado.id())
            ) {

                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Usuarios Service devolvió una respuesta inválida"
                );
            }

            return actualizado;

        } catch (RestClientResponseException e) {

            int codigo = e.getStatusCode().value();

            if (codigo == 400) {

                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre o correo ingresado no es válido"
                );
            }

            if (codigo == 409) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya pertenece a otra cuenta"
                );
            }

            if (codigo == 404) {

                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado"
                );
            }

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible actualizar el usuario"
            );

        } catch (RestClientException e) {

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Usuarios Service no está disponible"
            );
        }
    }

    // ========================================
    // OBTENER IDENTIFICADORES DE USUARIOS
    // ========================================

    private List<Long> obtenerUsuariosEmpresa(
        Long ejecutivoId,
        Long empresaId
    ) {

        String sql = """
            SELECT DISTINCT c.usuario_id
            FROM compromisos c
            INNER JOIN empresas e
                ON e.nombre = c.empresa
            INNER JOIN ejecutivos_empresas ee
                ON ee.empresa_id = e.id
            WHERE ee.ejecutivo_id = ?
              AND e.id = ?
            ORDER BY c.usuario_id
            """;

        return jdbcTemplate.queryForList(
            sql,
            Long.class,
            ejecutivoId,
            empresaId
        );
    }

    // ========================================
    // VALIDAR EMPRESA ASIGNADA
    // ========================================

    private void validarEmpresaAsignada(
        Long ejecutivoId,
        Long empresaId
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM ejecutivos_empresas ee
            INNER JOIN empresas e
                ON e.id = ee.empresa_id
            WHERE ee.ejecutivo_id = ?
              AND e.id = ?
            """;

        Integer cantidad =
            jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                ejecutivoId,
                empresaId
            );

        if (cantidad == null || cantidad == 0) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La empresa no está asignada al ejecutivo"
            );
        }
    }

    // ========================================
    // VALIDAR EJECUTIVO
    // ========================================

    private void validarEjecutivo(
        Long ejecutivoId
    ) {

        UsuarioRemotoResponse ejecutivo =
            consultarUsuario(ejecutivoId);

        if (
            ejecutivo == null ||
            !Boolean.TRUE.equals(ejecutivo.activo()) ||
            !"ADMINISTRADOR".equals(ejecutivo.rol())
        ) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La cuenta no tiene permisos de ejecutivo"
            );
        }
    }

    // ========================================
    // CONSULTAR USUARIOS SERVICE
    // ========================================

    private UsuarioRemotoResponse consultarUsuario(
        Long usuarioId
    ) {

        try {

            UsuarioRemotoResponse usuario =
                usuariosClient
                    .get()
                    .uri(
                        "/api/usuarios/{id}",
                        usuarioId
                    )
                    .retrieve()
                    .body(UsuarioRemotoResponse.class);

            if (
                usuario == null ||
                !usuarioId.equals(usuario.id())
            ) {

                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Usuarios Service devolvió una respuesta inválida"
                );
            }

            return usuario;

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 404) {

                return null;
            }

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible consultar Usuarios Service"
            );

        } catch (RestClientException e) {

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Usuarios Service no está disponible"
            );
        }
    }

    // ========================================
    // VALIDAR IDENTIFICADORES
    // ========================================

    private void validarIdentificador(
        Long id,
        String tipo
    ) {

        if (id == null || id <= 0L) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El identificador de " + tipo + " no es válido"
            );
        }
    }

    // ========================================
    // DATOS PERMITIDOS PARA ACTUALIZACIÓN
    // ========================================

    public record ActualizarUsuarioDatos(
        String nombre,
        String correo
    ) {
    }
}
