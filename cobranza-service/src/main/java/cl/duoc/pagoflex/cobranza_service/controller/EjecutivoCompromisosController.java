
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
public class EjecutivoCompromisosController {

    private final JdbcTemplate jdbcTemplate;

    public EjecutivoCompromisosController(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Obtener compromisos de las empresas del ejecutivo.
    @GetMapping("/{ejecutivoId}/compromisos")
    public List<CompromisoEjecutivoDto> listarCompromisos(
            @PathVariable Long ejecutivoId
    ) {

        if (ejecutivoId == null || ejecutivoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del ejecutivo no es valido"
            );
        }

        String sql = """
                SELECT
                    c.id,
                    c.usuario_id,
                    c.folio,
                    c.empresa,
                    c.concepto,
                    c.monto,
                    c.recargo,
                    c.fecha_vencimiento,
                    c.estado,
                    c.numero_cuota,
                    c.total_cuotas,
                    c.referencia_plan
                FROM compromisos c
                WHERE EXISTS (
                    SELECT 1
                    FROM empresas e
                    INNER JOIN ejecutivos_empresas ee
                        ON ee.empresa_id = e.id
                    WHERE ee.ejecutivo_id = ?
                      AND e.nombre = c.empresa
                )
                ORDER BY c.fecha_vencimiento, c.id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new CompromisoEjecutivoDto(
                        rs.getLong("id"),
                        rs.getLong("usuario_id"),
                        rs.getString("folio"),
                        rs.getString("empresa"),
                        rs.getString("concepto"),
                        rs.getLong("monto"),
                        rs.getLong("recargo"),
                        rs.getString("fecha_vencimiento"),
                        rs.getString("estado"),
                        rs.getObject("numero_cuota", Integer.class),
                        rs.getObject("total_cuotas", Integer.class),
                        rs.getString("referencia_plan")
                ),
                ejecutivoId
        );
    }

    // Datos devueltos a Android mediante Retrofit.
    public record CompromisoEjecutivoDto(
            Long id,
            Long usuarioId,
            String folio,
            String empresa,
            String concepto,
            Long monto,
            Long recargo,
            String fechaVencimiento,
            String estado,
            Integer numeroCuota,
            Integer totalCuotas,
            String referenciaPlan
    ) {
    }
}
