
package cl.duoc.pagoflex.cobranza_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cobranza/ejecutivos")
public class EjecutivoEmpresaController {

    private final JdbcTemplate jdbcTemplate;

    public EjecutivoEmpresaController(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Consultar las empresas asignadas a un ejecutivo.
    @GetMapping("/{ejecutivoId}/empresas")
    public List<EmpresaAsignadaDto> listarEmpresas(
            @PathVariable("ejecutivoId") Long ejecutivoId
    ) {

        if (ejecutivoId == null || ejecutivoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del ejecutivo no es válido"
            );
        }

        String sql = """
                SELECT
                    e.id,
                    e.nombre
                FROM ejecutivos_empresas ee
                INNER JOIN empresas e
                    ON e.id = ee.empresa_id
                WHERE ee.ejecutivo_id = ?
                ORDER BY e.nombre
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new EmpresaAsignadaDto(
                        rs.getLong("id"),
                        rs.getString("nombre")
                ),
                ejecutivoId
        );
    }

    public record EmpresaAsignadaDto(
            Long id,
            String nombre
    ) {
    }
}
