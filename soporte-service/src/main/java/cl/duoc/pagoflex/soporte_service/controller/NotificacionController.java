package cl.duoc.pagoflex.soporte_service.controller;

import cl.duoc.pagoflex.soporte_service.dto.CrearNotificacionRequest;
import cl.duoc.pagoflex.soporte_service.dto.NotificacionResponse;
import cl.duoc.pagoflex.soporte_service.service.NotificacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soporte/notificaciones")
public class NotificacionController {
    private final NotificacionService servicio;
    private final String claveInterna;

    public NotificacionController(NotificacionService servicio,
        @Value("${pagoflex.internal.key}") String claveInterna) {
        this.servicio = servicio;
        this.claveInterna = claveInterna;
    }

    @GetMapping
    public List<NotificacionResponse> listar(@RequestParam Long usuarioId) {
        return servicio.listar(usuarioId);
    }

    @GetMapping("/no-leidas")
    public Map<String, Long> noLeidas(@RequestParam Long usuarioId) {
        return Map.of("cantidad", servicio.noLeidas(usuarioId));
    }

    @PutMapping("/{id}/leer")
    public NotificacionResponse leer(@PathVariable Long id, @RequestParam Long usuarioId) {
        return servicio.marcarLeida(usuarioId, id);
    }

    @PutMapping("/leer-todas")
    public Map<String, Integer> leerTodas(@RequestParam Long usuarioId) {
        return Map.of("cantidad", servicio.marcarTodas(usuarioId));
    }

    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionResponse crearEvento(@RequestHeader("X-PagoFlex-Internal-Key") String clave,
                                             @RequestBody CrearNotificacionRequest datos) {
        if (!MessageDigest.isEqual(claveInterna.getBytes(StandardCharsets.UTF_8),
                                    clave.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso interno no autorizado");
        }
        return servicio.crear(datos);
    }
}
