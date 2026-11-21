
package cl.duoc.pagoflex.cobranza_service.controller;

import cl.duoc.pagoflex.cobranza_service.dto.CompromisoResponse;
import cl.duoc.pagoflex.cobranza_service.dto.RegistrarCompromisoRequest;
import cl.duoc.pagoflex.cobranza_service.service.CobranzaService;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza")
public class CobranzaController {

    private final CobranzaService cobranzaService;
    private final JdbcTemplate jdbcTemplate;

    public CobranzaController(
        CobranzaService cobranzaService,
        JdbcTemplate jdbcTemplate
    ) {
        this.cobranzaService = cobranzaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // REGISTRO ORIGINAL DE COMPROMISOS.
    // Se conserva por compatibilidad con las pruebas anteriores.

    @PostMapping("/compromisos")
    @ResponseStatus(HttpStatus.CREATED)
    public CompromisoResponse registrarCompromiso(
        @RequestBody RegistrarCompromisoRequest datos
    ) {
        return cobranzaService.registrarCompromiso(
            datos
        );
    }

    // NUEVO: REGISTRO DE COMPROMISOS DEL EJECUTIVO.

    @PostMapping("/ejecutivos/{ejecutivoId}/compromisos")
    @ResponseStatus(HttpStatus.CREATED)
    public CompromisoResponse registrarCompromisoEjecutivo(
        @PathVariable("ejecutivoId") Long ejecutivoId,
        @RequestBody RegistrarCompromisoRequest datos
    ) {

        // Validar ID del ejecutivo.
        if (ejecutivoId == null || ejecutivoId <= 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El ID del ejecutivo no es valido"
            );
        }

        // Validar que se haya indicado una empresa.
        if (
            datos == null ||
            datos.empresa() == null ||
            datos.empresa().isBlank()
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La empresa es obligatoria"
            );
        }

        // Comprobar la asociacion en MySQL.
        String sql = """
            SELECT COUNT(*)
            FROM ejecutivos_empresas ee
            INNER JOIN empresas e
                ON e.id = ee.empresa_id
            WHERE ee.ejecutivo_id = ?
              AND e.nombre = ?
            """;

        Integer cantidad = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            ejecutivoId,
            datos.empresa().trim()
        );

        // Rechazar empresas ajenas al ejecutivo.
        if (cantidad == null || cantidad == 0) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La empresa no esta asignada al ejecutivo"
            );
        }

        // Utilizar el servicio original.
        // Conserva sus validaciones y generacion de folios.
        return cobranzaService.registrarCompromiso(
            datos
        );
    }

    // CONSULTAR COMPROMISOS DE UN USUARIO.

    @GetMapping("/compromisos")
    public List<CompromisoResponse> listarCompromisos(
        @RequestParam Long usuarioId
    ) {

        return cobranzaService.listarCompromisos(
            usuarioId
        );
    }

    // OBTENER COMPROMISO POR FOLIO.

    @GetMapping("/compromisos/{folio}")
    public CompromisoResponse obtenerCompromiso(
        @PathVariable String folio,
        @RequestParam Long usuarioId
    ) {

        return cobranzaService.obtenerCompromiso(
            usuarioId,
            folio
        );
    }

    // LISTAR EMPRESAS.

    @GetMapping("/empresas")
    public List<String> listarEmpresas() {

        return cobranzaService.listarEmpresas();
    }
}
