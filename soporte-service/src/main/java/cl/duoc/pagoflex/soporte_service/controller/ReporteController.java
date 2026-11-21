package cl.duoc.pagoflex.soporte_service.controller;

import cl.duoc.pagoflex.soporte_service.dto.CrearReporteRequest;
import cl.duoc.pagoflex.soporte_service.dto.ReporteResponse;
import cl.duoc.pagoflex.soporte_service.dto.ResolverReporteRequest;
import cl.duoc.pagoflex.soporte_service.dto.TomarReporteRequest;
import cl.duoc.pagoflex.soporte_service.model.EstadoReporte;
import cl.duoc.pagoflex.soporte_service.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/soporte/reportes")
public class ReporteController {
    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) { this.servicio = servicio; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse crear(@RequestBody CrearReporteRequest datos) {
        return servicio.crear(datos);
    }

    @GetMapping
    public List<ReporteResponse> listar(@RequestParam Long usuarioId) {
        return servicio.listarUsuario(usuarioId);
    }

    @GetMapping("/{codigo}")
    public ReporteResponse obtener(@PathVariable String codigo, @RequestParam Long usuarioId) {
        return servicio.obtenerUsuario(usuarioId, codigo);
    }

    @GetMapping("/gestion")
    public List<ReporteResponse> gestion(@RequestParam Long agenteId,
                                         @RequestParam(required = false) EstadoReporte estado) {
        return servicio.listarGestion(agenteId, estado);
    }

    @PostMapping("/{id}/tomar")
    public ReporteResponse tomar(@PathVariable Long id, @RequestBody TomarReporteRequest datos) {
        return servicio.tomar(id, datos);
    }

    @PostMapping("/{id}/resolver")
    public ReporteResponse resolver(@PathVariable Long id, @RequestBody ResolverReporteRequest datos) {
        return servicio.resolver(id, datos);
    }
}
